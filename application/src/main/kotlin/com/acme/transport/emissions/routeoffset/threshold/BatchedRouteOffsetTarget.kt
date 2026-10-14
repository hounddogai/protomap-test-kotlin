package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched route offset target is in its life cycle. */
enum class BatchedRouteOffsetTargetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a batched route offset target and its current status. */
data class BatchedRouteOffsetTarget(
    var code: String,
    var label: String,
    var currency: String,
    var maximum: Double,
    var amount: Double,
    var status: BatchedRouteOffsetTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRouteOffsetTargetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRouteOffsetTargetStatus): Boolean {
        when (status) {
            BatchedRouteOffsetTargetStatus.OPEN -> {
                when (next) {
                    BatchedRouteOffsetTargetStatus.RESOLVED, BatchedRouteOffsetTargetStatus.REOPENED -> return true
                }
                return false
            }
            BatchedRouteOffsetTargetStatus.IN_REVIEW -> return next == BatchedRouteOffsetTargetStatus.RESOLVED || next == BatchedRouteOffsetTargetStatus.ARCHIVED
            BatchedRouteOffsetTargetStatus.RESOLVED -> {
                when (next) {
                    BatchedRouteOffsetTargetStatus.REOPENED, BatchedRouteOffsetTargetStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedRouteOffsetTargetStatus.REOPENED -> {
                when (next) {
                    BatchedRouteOffsetTargetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedRouteOffsetTargetStatus) {
        if (!canMoveTo(next)) {
            error("batched route offset target $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netExposure(): Double = round((amount - maximum) * 100) / 100

    fun snapshot(): BatchedRouteOffsetTarget {
        return BatchedRouteOffsetTarget(
            code = code,
            label = label,
            currency = currency,
            maximum = maximum,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched route offset target in status open. */
fun newBatchedRouteOffsetTarget(code: String, label: String): BatchedRouteOffsetTarget {
    return BatchedRouteOffsetTarget(
        code = code,
        label = label,
        currency = "",
        maximum = 0.0,
        amount = 0.0,
        status = BatchedRouteOffsetTargetStatus.OPEN,
    )
}
