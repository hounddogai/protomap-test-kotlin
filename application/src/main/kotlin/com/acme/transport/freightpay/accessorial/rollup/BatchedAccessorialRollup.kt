package com.acme.transport.freightpay.accessorial.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched accessorial rollup records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a batched accessorial rollup is in its life cycle. */
enum class BatchedAccessorialRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched accessorial rollup. Its code is unique within a tenant. */
data class BatchedAccessorialRollup(
    var code: String,
    var title: String,
    var cost: Double,
    var discount: Double,
    var summary: String,
    var startsAt: Int,
    var status: BatchedAccessorialRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedAccessorialRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedAccessorialRollupStatus): Boolean {
        when (status) {
            BatchedAccessorialRollupStatus.OPEN -> return next == BatchedAccessorialRollupStatus.IN_REVIEW || next == BatchedAccessorialRollupStatus.RESOLVED
            BatchedAccessorialRollupStatus.IN_REVIEW -> return next == BatchedAccessorialRollupStatus.RESOLVED || next == BatchedAccessorialRollupStatus.ARCHIVED
            BatchedAccessorialRollupStatus.RESOLVED -> {
                when (next) {
                    BatchedAccessorialRollupStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedAccessorialRollupStatus.REOPENED -> {
                when (next) {
                    BatchedAccessorialRollupStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedAccessorialRollupStatus) {
        if (!canMoveTo(next)) {
            error("batched accessorial rollup $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"batched accessorial rollup"} $code"

    fun adjustedVolume(): Double = round(discount * cost * 100) / 100

    fun snapshot(): BatchedAccessorialRollup {
        return BatchedAccessorialRollup(
            code = code,
            title = title,
            cost = cost,
            discount = discount,
            summary = summary,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched accessorial rollup in status open. */
fun newBatchedAccessorialRollup(code: String, title: String): BatchedAccessorialRollup {
    return BatchedAccessorialRollup(
        code = code,
        title = title,
        cost = 0.0,
        discount = 0.0,
        summary = "",
        startsAt = 0,
        status = BatchedAccessorialRollupStatus.OPEN,
    )
}
