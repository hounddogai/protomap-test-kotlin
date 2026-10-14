package com.acme.transport.terminal.traileryard.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling trailer yard allocation is in its life cycle. */
enum class RollingTrailerYardAllocationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a rolling trailer yard allocation and its current status. */
data class RollingTrailerYardAllocation(
    var code: String,
    var name: String,
    var budget: Double,
    var ratio: Double,
    var priority: Int,
    var lane: String,
    var floorValue: Double,
    var status: RollingTrailerYardAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingTrailerYardAllocationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingTrailerYardAllocationStatus): Boolean {
        when (status) {
            RollingTrailerYardAllocationStatus.PROPOSED -> {
                when (next) {
                    RollingTrailerYardAllocationStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingTrailerYardAllocationStatus.APPROVED -> return next == RollingTrailerYardAllocationStatus.WITHDRAWN
            RollingTrailerYardAllocationStatus.REJECTED -> {
                when (next) {
                    RollingTrailerYardAllocationStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingTrailerYardAllocationStatus) {
        if (!canMoveTo(next)) {
            error("rolling trailer yard allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"rolling trailer yard allocation"} $code"

    fun projectedValue(): Double = round((ratio - budget) * 100) / 100

    fun snapshot(): RollingTrailerYardAllocation {
        return RollingTrailerYardAllocation(
            code = code,
            name = name,
            budget = budget,
            ratio = ratio,
            priority = priority,
            lane = lane,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a rolling trailer yard allocation in status proposed. */
fun newRollingTrailerYardAllocation(code: String, name: String): RollingTrailerYardAllocation {
    return RollingTrailerYardAllocation(
        code = code,
        name = name,
        budget = 0.0,
        ratio = 0.0,
        priority = 0,
        lane = "",
        floorValue = 0.0,
        status = RollingTrailerYardAllocationStatus.PROPOSED,
    )
}
