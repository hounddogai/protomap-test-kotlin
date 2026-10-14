package com.acme.transport.emissions.routeoffset.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset milestone is in its life cycle. */
enum class RouteOffsetMilestoneStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a route offset milestone and its current status. */
data class RouteOffsetMilestone(
    var code: String,
    var title: String,
    var phone: String,
    var minimum: Double,
    var label: String,
    var durationMinutes: Int,
    var score: Double,
    var status: RouteOffsetMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetMilestoneStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetMilestoneStatus): Boolean {
        when (status) {
            RouteOffsetMilestoneStatus.PLANNED -> {
                when (next) {
                    RouteOffsetMilestoneStatus.SCHEDULED -> return true
                }
                return false
            }
            RouteOffsetMilestoneStatus.SCHEDULED -> {
                when (next) {
                    RouteOffsetMilestoneStatus.COMPLETED, RouteOffsetMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
            RouteOffsetMilestoneStatus.IN_PROGRESS -> return next == RouteOffsetMilestoneStatus.CANCELLED
            RouteOffsetMilestoneStatus.COMPLETED -> return next == RouteOffsetMilestoneStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RouteOffsetMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("route offset milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$title]"

    fun effectiveExposure(): Double = round((minimum - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): RouteOffsetMilestone {
        return RouteOffsetMilestone(
            code = code,
            title = title,
            phone = phone,
            minimum = minimum,
            label = label,
            durationMinutes = durationMinutes,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a route offset milestone in status planned. */
fun newRouteOffsetMilestone(code: String, title: String): RouteOffsetMilestone {
    return RouteOffsetMilestone(
        code = code,
        title = title,
        phone = "",
        minimum = 0.0,
        label = "",
        durationMinutes = 0,
        score = 0.0,
        status = RouteOffsetMilestoneStatus.PLANNED,
    )
}
