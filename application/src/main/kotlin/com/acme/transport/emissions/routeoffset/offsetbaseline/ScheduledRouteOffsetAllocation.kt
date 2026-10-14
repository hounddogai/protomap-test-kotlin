package com.acme.transport.emissions.routeoffset.offsetbaseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled route offset allocation is in its life cycle. */
enum class ScheduledRouteOffsetAllocationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One scheduled route offset allocation, with what the planners need to act on it. */
data class ScheduledRouteOffsetAllocation(
    var code: String,
    var name: String,
    var updatedAt: Int,
    var channel: String,
    var trailerId: String,
    var etaMinutes: Int,
    var status: ScheduledRouteOffsetAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledRouteOffsetAllocationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledRouteOffsetAllocationStatus): Boolean {
        when (status) {
            ScheduledRouteOffsetAllocationStatus.CREATED -> return next == ScheduledRouteOffsetAllocationStatus.PUBLISHED || next == ScheduledRouteOffsetAllocationStatus.RETIRED
            ScheduledRouteOffsetAllocationStatus.VALIDATED -> {
                when (next) {
                    ScheduledRouteOffsetAllocationStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledRouteOffsetAllocationStatus.PUBLISHED -> {
                when (next) {
                    ScheduledRouteOffsetAllocationStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledRouteOffsetAllocationStatus) {
        if (!canMoveTo(next)) {
            error("scheduled route offset allocation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"scheduled route offset allocation"} $code"

    fun netVolume(): Double = round((etaMinutes.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledRouteOffsetAllocation {
        return ScheduledRouteOffsetAllocation(
            code = code,
            name = name,
            updatedAt = updatedAt,
            channel = channel,
            trailerId = trailerId,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a scheduled route offset allocation in status created. */
fun newScheduledRouteOffsetAllocation(code: String, name: String): ScheduledRouteOffsetAllocation {
    return ScheduledRouteOffsetAllocation(
        code = code,
        name = name,
        updatedAt = 0,
        channel = "",
        trailerId = "",
        etaMinutes = 0,
        status = ScheduledRouteOffsetAllocationStatus.CREATED,
    )
}
