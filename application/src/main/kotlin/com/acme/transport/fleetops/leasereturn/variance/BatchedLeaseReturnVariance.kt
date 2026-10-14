package com.acme.transport.fleetops.leasereturn.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched lease return variance records of fleetops's lease return work, and the rules, stores, and
// calculations around them.

/** Where a batched lease return variance is in its life cycle. */
enum class BatchedLeaseReturnVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One batched lease return variance, with what the planners need to act on it. */
data class BatchedLeaseReturnVariance(
    var code: String,
    var label: String,
    var pinned: Boolean,
    var tags: MutableList<String>,
    var region: String,
    var locale: String,
    var updatedAt: Int,
    var budget: Double,
    var graceDays: Int,
    var status: BatchedLeaseReturnVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedLeaseReturnVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedLeaseReturnVarianceStatus): Boolean {
        when (status) {
            BatchedLeaseReturnVarianceStatus.IDLE -> return next == BatchedLeaseReturnVarianceStatus.DRAINING
            BatchedLeaseReturnVarianceStatus.WARMING -> {
                when (next) {
                    BatchedLeaseReturnVarianceStatus.READY, BatchedLeaseReturnVarianceStatus.DRAINING -> return true
                }
                return false
            }
            BatchedLeaseReturnVarianceStatus.READY -> {
                when (next) {
                    BatchedLeaseReturnVarianceStatus.DRAINING, BatchedLeaseReturnVarianceStatus.STOPPED -> return true
                }
                return false
            }
            BatchedLeaseReturnVarianceStatus.DRAINING -> return next == BatchedLeaseReturnVarianceStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BatchedLeaseReturnVarianceStatus) {
        if (!canMoveTo(next)) {
            error("batched lease return variance $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedVolume(): Double = round(budget * graceDays.toDouble() * 100) / 100

    fun snapshot(): BatchedLeaseReturnVariance {
        return BatchedLeaseReturnVariance(
            code = code,
            label = label,
            pinned = pinned,
            tags = tags,
            region = region,
            locale = locale,
            updatedAt = updatedAt,
            budget = budget,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a batched lease return variance in status idle. */
fun newBatchedLeaseReturnVariance(code: String, label: String): BatchedLeaseReturnVariance {
    return BatchedLeaseReturnVariance(
        code = code,
        label = label,
        pinned = false,
        tags = mutableListOf<String>(),
        region = "",
        locale = "",
        updatedAt = 0,
        budget = 0.0,
        graceDays = 0,
        status = BatchedLeaseReturnVarianceStatus.IDLE,
    )
}
