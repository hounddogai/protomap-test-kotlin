package com.acme.transport.emissions.routeoffset.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset schedule is in its life cycle. */
enum class RouteOffsetScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A route offset schedule as the route offset team keeps it. */
data class RouteOffsetSchedule(
    var code: String,
    var title: String,
    var revision: Int,
    var endsAt: Int,
    var checksum: String,
    var status: RouteOffsetScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetScheduleStatus): Boolean {
        when (status) {
            RouteOffsetScheduleStatus.DRAFT -> {
                when (next) {
                    RouteOffsetScheduleStatus.SUSPENDED, RouteOffsetScheduleStatus.CLOSED -> return true
                }
                return false
            }
            RouteOffsetScheduleStatus.PENDING -> return next == RouteOffsetScheduleStatus.CLOSED
            RouteOffsetScheduleStatus.ACTIVE -> {
                when (next) {
                    RouteOffsetScheduleStatus.CLOSED -> return true
                }
                return false
            }
            RouteOffsetScheduleStatus.SUSPENDED -> return next == RouteOffsetScheduleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RouteOffsetScheduleStatus) {
        if (!canMoveTo(next)) {
            error("route offset schedule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun label(): String = "${"route offset schedule"} $code"

    fun effectiveTotal(): Double = round(revision.toDouble() * endsAt.toDouble() * 100) / 100

    fun snapshot(): RouteOffsetSchedule {
        return RouteOffsetSchedule(
            code = code,
            title = title,
            revision = revision,
            endsAt = endsAt,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a route offset schedule in status draft. */
fun newRouteOffsetSchedule(code: String, title: String): RouteOffsetSchedule {
    return RouteOffsetSchedule(
        code = code,
        title = title,
        revision = 0,
        endsAt = 0,
        checksum = "",
        status = RouteOffsetScheduleStatus.DRAFT,
    )
}
