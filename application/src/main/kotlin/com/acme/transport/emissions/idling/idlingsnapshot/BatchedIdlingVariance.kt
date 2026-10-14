package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched idling variance is in its life cycle. */
enum class BatchedIdlingVarianceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A batched idling variance. Its code is unique within a tenant. */
data class BatchedIdlingVariance(
    var code: String,
    var name: String,
    var cost: Double,
    var sequence: Int,
    var margin: Double,
    var notes: String,
    var startsAt: Int,
    var ceiling: Double,
    var amount: Double,
    var tags: MutableList<String>,
    var status: BatchedIdlingVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedIdlingVarianceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedIdlingVarianceStatus): Boolean {
        when (status) {
            BatchedIdlingVarianceStatus.CREATED -> return next == BatchedIdlingVarianceStatus.VALIDATED || next == BatchedIdlingVarianceStatus.RETIRED
            BatchedIdlingVarianceStatus.VALIDATED -> {
                when (next) {
                    BatchedIdlingVarianceStatus.RETIRED -> return true
                }
                return false
            }
            BatchedIdlingVarianceStatus.PUBLISHED -> return next == BatchedIdlingVarianceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: BatchedIdlingVarianceStatus) {
        if (!canMoveTo(next)) {
            error("batched idling variance $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedVolume(): Double = round((margin - cost) * 100) / 100

    fun snapshot(): BatchedIdlingVariance {
        return BatchedIdlingVariance(
            code = code,
            name = name,
            cost = cost,
            sequence = sequence,
            margin = margin,
            notes = notes,
            startsAt = startsAt,
            ceiling = ceiling,
            amount = amount,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched idling variance in status created. */
fun newBatchedIdlingVariance(code: String, name: String): BatchedIdlingVariance {
    return BatchedIdlingVariance(
        code = code,
        name = name,
        cost = 0.0,
        sequence = 0,
        margin = 0.0,
        notes = "",
        startsAt = 0,
        ceiling = 0.0,
        amount = 0.0,
        tags = mutableListOf<String>(),
        status = BatchedIdlingVarianceStatus.CREATED,
    )
}
