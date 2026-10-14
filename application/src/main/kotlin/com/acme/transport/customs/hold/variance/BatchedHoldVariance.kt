package com.acme.transport.customs.hold.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched hold variance is in its life cycle. */
enum class BatchedHoldVarianceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched hold variance as the hold team keeps it. */
data class BatchedHoldVariance(
    var code: String,
    var name: String,
    var tags: MutableList<String>,
    var minimum: Double,
    var phone: String,
    var cost: Double,
    var revision: Int,
    var stopCount: Int,
    var status: BatchedHoldVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedHoldVarianceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedHoldVarianceStatus): Boolean {
        when (status) {
            BatchedHoldVarianceStatus.PROPOSED -> return next == BatchedHoldVarianceStatus.APPROVED || next == BatchedHoldVarianceStatus.REJECTED
            BatchedHoldVarianceStatus.APPROVED -> return next == BatchedHoldVarianceStatus.REJECTED
            BatchedHoldVarianceStatus.REJECTED -> return next == BatchedHoldVarianceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedHoldVarianceStatus) {
        if (!canMoveTo(next)) {
            error("batched hold variance $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"batched hold variance"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netVolume(): Double = round(cost * stopCount.toDouble() * 100) / 100

    fun snapshot(): BatchedHoldVariance {
        return BatchedHoldVariance(
            code = code,
            name = name,
            tags = tags,
            minimum = minimum,
            phone = phone,
            cost = cost,
            revision = revision,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched hold variance in status proposed. */
fun newBatchedHoldVariance(code: String, name: String): BatchedHoldVariance {
    return BatchedHoldVariance(
        code = code,
        name = name,
        tags = mutableListOf<String>(),
        minimum = 0.0,
        phone = "",
        cost = 0.0,
        revision = 0,
        stopCount = 0,
        status = BatchedHoldVarianceStatus.PROPOSED,
    )
}
