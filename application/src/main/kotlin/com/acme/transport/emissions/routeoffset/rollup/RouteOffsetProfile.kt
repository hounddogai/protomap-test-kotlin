package com.acme.transport.emissions.routeoffset.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a route offset profile is in its life cycle. */
enum class RouteOffsetProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A route offset profile as the route offset team keeps it. */
data class RouteOffsetProfile(
    var code: String,
    var label: String,
    var routeCode: String,
    var ownerId: String,
    var depot: String,
    var odometer: Int,
    var updatedAt: Int,
    var phone: String,
    var tags: MutableList<String>,
    var attributes: MutableMap<String, String>,
    var status: RouteOffsetProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetProfileStatus): Boolean {
        when (status) {
            RouteOffsetProfileStatus.DRAFT -> return next == RouteOffsetProfileStatus.PENDING || next == RouteOffsetProfileStatus.ACTIVE
            RouteOffsetProfileStatus.PENDING -> return next == RouteOffsetProfileStatus.ACTIVE || next == RouteOffsetProfileStatus.CLOSED
            RouteOffsetProfileStatus.ACTIVE -> {
                when (next) {
                    RouteOffsetProfileStatus.SUSPENDED -> return true
                }
                return false
            }
            RouteOffsetProfileStatus.SUSPENDED -> {
                when (next) {
                    RouteOffsetProfileStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetProfileStatus) {
        if (!canMoveTo(next)) {
            error("route offset profile $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedVolume(): Double = round(updatedAt.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): RouteOffsetProfile {
        return RouteOffsetProfile(
            code = code,
            label = label,
            routeCode = routeCode,
            ownerId = ownerId,
            depot = depot,
            odometer = odometer,
            updatedAt = updatedAt,
            phone = phone,
            tags = tags,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a route offset profile in status draft. */
fun newRouteOffsetProfile(code: String, label: String): RouteOffsetProfile {
    return RouteOffsetProfile(
        code = code,
        label = label,
        routeCode = "",
        ownerId = "",
        depot = "",
        odometer = 0,
        updatedAt = 0,
        phone = "",
        tags = mutableListOf<String>(),
        attributes = mutableMapOf<String, String>(),
        status = RouteOffsetProfileStatus.DRAFT,
    )
}
