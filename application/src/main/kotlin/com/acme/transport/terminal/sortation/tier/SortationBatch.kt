package com.acme.transport.terminal.sortation.tier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a sortation batch is in its life cycle. */
enum class SortationBatchStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One sortation batch, with what the planners need to act on it. */
data class SortationBatch(
    var code: String,
    var name: String,
    var rate: Double,
    var revision: Int,
    var trailerId: String,
    var phone: String,
    var cost: Double,
    var threshold: Double,
    var status: SortationBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationBatchStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationBatchStatus): Boolean {
        when (status) {
            SortationBatchStatus.REQUESTED -> return next == SortationBatchStatus.GRANTED || next == SortationBatchStatus.REVOKED
            SortationBatchStatus.GRANTED -> return next == SortationBatchStatus.REVOKED
            SortationBatchStatus.REVOKED -> {
                when (next) {
                    SortationBatchStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SortationBatchStatus) {
        if (!canMoveTo(next)) {
            error("sortation batch $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code / $name"

    fun adjustedExposure(): Double = round((rate - revision.toDouble()) * 100) / 100

    fun snapshot(): SortationBatch {
        return SortationBatch(
            code = code,
            name = name,
            rate = rate,
            revision = revision,
            trailerId = trailerId,
            phone = phone,
            cost = cost,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a sortation batch in status requested. */
fun newSortationBatch(code: String, name: String): SortationBatch {
    return SortationBatch(
        code = code,
        name = name,
        rate = 0.0,
        revision = 0,
        trailerId = "",
        phone = "",
        cost = 0.0,
        threshold = 0.0,
        status = SortationBatchStatus.REQUESTED,
    )
}
