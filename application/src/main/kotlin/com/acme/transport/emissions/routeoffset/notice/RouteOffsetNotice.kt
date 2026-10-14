package com.acme.transport.emissions.routeoffset.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The route offset notice records of emissions's route offset work, and the rules, stores, and calculations
// around them.

/** Where a route offset notice is in its life cycle. */
enum class RouteOffsetNoticeStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A route offset notice as the route offset team keeps it. */
data class RouteOffsetNotice(
    var code: String,
    var label: String,
    var attempts: Int,
    var discount: Double,
    var rate: Double,
    var title: String,
    var status: RouteOffsetNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetNoticeStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetNoticeStatus): Boolean {
        when (status) {
            RouteOffsetNoticeStatus.PLANNED -> return next == RouteOffsetNoticeStatus.COMPLETED
            RouteOffsetNoticeStatus.SCHEDULED -> return next == RouteOffsetNoticeStatus.CANCELLED
            RouteOffsetNoticeStatus.IN_PROGRESS -> {
                when (next) {
                    RouteOffsetNoticeStatus.COMPLETED, RouteOffsetNoticeStatus.CANCELLED -> return true
                }
                return false
            }
            RouteOffsetNoticeStatus.COMPLETED -> return next == RouteOffsetNoticeStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RouteOffsetNoticeStatus) {
        if (!canMoveTo(next)) {
            error("route offset notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedValue(): Double = round(attempts.toDouble() * rate * 100) / 100

    fun snapshot(): RouteOffsetNotice {
        return RouteOffsetNotice(
            code = code,
            label = label,
            attempts = attempts,
            discount = discount,
            rate = rate,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a route offset notice in status planned. */
fun newRouteOffsetNotice(code: String, label: String): RouteOffsetNotice {
    return RouteOffsetNotice(
        code = code,
        label = label,
        attempts = 0,
        discount = 0.0,
        rate = 0.0,
        title = "",
        status = RouteOffsetNoticeStatus.PLANNED,
    )
}
