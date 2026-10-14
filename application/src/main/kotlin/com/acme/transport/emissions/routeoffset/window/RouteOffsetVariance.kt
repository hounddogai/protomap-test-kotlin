package com.acme.transport.emissions.routeoffset.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset variance is in its life cycle. */
enum class RouteOffsetVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A route offset variance. Its code is unique within a tenant. */
data class RouteOffsetVariance(
    var code: String,
    var name: String,
    var ratio: Double,
    var pinned: Boolean,
    var timezone: String,
    var routeCode: String,
    var country: String,
    var stopCount: Int,
    var phone: String,
    var status: RouteOffsetVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetVarianceStatus): Boolean {
        when (status) {
            RouteOffsetVarianceStatus.OPEN -> {
                when (next) {
                    RouteOffsetVarianceStatus.REOPENED, RouteOffsetVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
            RouteOffsetVarianceStatus.IN_REVIEW -> {
                when (next) {
                    RouteOffsetVarianceStatus.REOPENED, RouteOffsetVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
            RouteOffsetVarianceStatus.RESOLVED -> return next == RouteOffsetVarianceStatus.ARCHIVED
            RouteOffsetVarianceStatus.REOPENED -> return next == RouteOffsetVarianceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RouteOffsetVarianceStatus) {
        if (!canMoveTo(next)) {
            error("route offset variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun netVolume(): Double = round((ratio + stopCount.toDouble()) * 100) / 100

    fun snapshot(): RouteOffsetVariance {
        return RouteOffsetVariance(
            code = code,
            name = name,
            ratio = ratio,
            pinned = pinned,
            timezone = timezone,
            routeCode = routeCode,
            country = country,
            stopCount = stopCount,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a route offset variance in status open. */
fun newRouteOffsetVariance(code: String, name: String): RouteOffsetVariance {
    return RouteOffsetVariance(
        code = code,
        name = name,
        ratio = 0.0,
        pinned = false,
        timezone = "",
        routeCode = "",
        country = "",
        stopCount = 0,
        phone = "",
        status = RouteOffsetVarianceStatus.OPEN,
    )
}
