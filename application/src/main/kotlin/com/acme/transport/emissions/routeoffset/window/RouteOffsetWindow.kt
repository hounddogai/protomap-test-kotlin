package com.acme.transport.emissions.routeoffset.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The route offset window records of emissions's route offset work, and the rules, stores, and calculations
// around them.

/** Where a route offset window is in its life cycle. */
enum class RouteOffsetWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One route offset window, with what the planners need to act on it. */
data class RouteOffsetWindow(
    var code: String,
    var title: String,
    var startsAt: Int,
    var tags: MutableList<String>,
    var timezone: String,
    var maximum: Double,
    var floorValue: Double,
    var reviewer: String,
    var contactEmail: String,
    var status: RouteOffsetWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetWindowStatus): Boolean {
        when (status) {
            RouteOffsetWindowStatus.OPEN -> {
                when (next) {
                    RouteOffsetWindowStatus.IN_REVIEW, RouteOffsetWindowStatus.ARCHIVED -> return true
                }
                return false
            }
            RouteOffsetWindowStatus.IN_REVIEW -> return next == RouteOffsetWindowStatus.RESOLVED
            RouteOffsetWindowStatus.RESOLVED -> {
                when (next) {
                    RouteOffsetWindowStatus.REOPENED -> return true
                }
                return false
            }
            RouteOffsetWindowStatus.REOPENED -> {
                when (next) {
                    RouteOffsetWindowStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetWindowStatus) {
        if (!canMoveTo(next)) {
            error("route offset window $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"route offset window"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netVolume(): Double = round((floorValue - maximum) * 100) / 100

    fun snapshot(): RouteOffsetWindow {
        return RouteOffsetWindow(
            code = code,
            title = title,
            startsAt = startsAt,
            tags = tags,
            timezone = timezone,
            maximum = maximum,
            floorValue = floorValue,
            reviewer = reviewer,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a route offset window in status open. */
fun newRouteOffsetWindow(code: String, title: String): RouteOffsetWindow {
    return RouteOffsetWindow(
        code = code,
        title = title,
        startsAt = 0,
        tags = mutableListOf<String>(),
        timezone = "",
        maximum = 0.0,
        floorValue = 0.0,
        reviewer = "",
        contactEmail = "",
        status = RouteOffsetWindowStatus.OPEN,
    )
}
