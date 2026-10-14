package com.acme.transport.emissions.routeoffset.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched route offset allocation is in its life cycle. */
enum class BatchedRouteOffsetAllocationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One batched route offset allocation, with what the planners need to act on it. */
data class BatchedRouteOffsetAllocation(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var durationMinutes: Int,
    var maximum: Double,
    var status: BatchedRouteOffsetAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRouteOffsetAllocationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRouteOffsetAllocationStatus): Boolean {
        when (status) {
            BatchedRouteOffsetAllocationStatus.IDLE -> {
                when (next) {
                    BatchedRouteOffsetAllocationStatus.WARMING, BatchedRouteOffsetAllocationStatus.STOPPED -> return true
                }
                return false
            }
            BatchedRouteOffsetAllocationStatus.WARMING -> return next == BatchedRouteOffsetAllocationStatus.DRAINING
            BatchedRouteOffsetAllocationStatus.READY -> return next == BatchedRouteOffsetAllocationStatus.DRAINING
            BatchedRouteOffsetAllocationStatus.DRAINING -> {
                when (next) {
                    BatchedRouteOffsetAllocationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedRouteOffsetAllocationStatus) {
        if (!canMoveTo(next)) {
            error("batched route offset allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched route offset allocation"} $code"

    fun effectiveValue(): Double = round((fuelLiters + maximum) * 100) / 100

    fun snapshot(): BatchedRouteOffsetAllocation {
        return BatchedRouteOffsetAllocation(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            durationMinutes = durationMinutes,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched route offset allocation in status idle. */
fun newBatchedRouteOffsetAllocation(code: String, title: String): BatchedRouteOffsetAllocation {
    return BatchedRouteOffsetAllocation(
        code = code,
        title = title,
        fuelLiters = 0.0,
        durationMinutes = 0,
        maximum = 0.0,
        status = BatchedRouteOffsetAllocationStatus.IDLE,
    )
}
