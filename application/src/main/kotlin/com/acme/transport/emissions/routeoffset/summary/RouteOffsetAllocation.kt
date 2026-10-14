package com.acme.transport.emissions.routeoffset.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset allocation is in its life cycle. */
enum class RouteOffsetAllocationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One route offset allocation, with what the planners need to act on it. */
data class RouteOffsetAllocation(
    var code: String,
    var title: String,
    var windowDays: Int,
    var visible: Boolean,
    var updatedAt: Int,
    var status: RouteOffsetAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetAllocationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetAllocationStatus): Boolean {
        when (status) {
            RouteOffsetAllocationStatus.PROPOSED -> return next == RouteOffsetAllocationStatus.APPROVED || next == RouteOffsetAllocationStatus.REJECTED
            RouteOffsetAllocationStatus.APPROVED -> {
                when (next) {
                    RouteOffsetAllocationStatus.REJECTED -> return true
                }
                return false
            }
            RouteOffsetAllocationStatus.REJECTED -> {
                when (next) {
                    RouteOffsetAllocationStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetAllocationStatus) {
        if (!canMoveTo(next)) {
            error("route offset allocation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netTotal(): Double = round((windowDays.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): RouteOffsetAllocation {
        return RouteOffsetAllocation(
            code = code,
            title = title,
            windowDays = windowDays,
            visible = visible,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a route offset allocation in status proposed. */
fun newRouteOffsetAllocation(code: String, title: String): RouteOffsetAllocation {
    return RouteOffsetAllocation(
        code = code,
        title = title,
        windowDays = 0,
        visible = false,
        updatedAt = 0,
        status = RouteOffsetAllocationStatus.PROPOSED,
    )
}
