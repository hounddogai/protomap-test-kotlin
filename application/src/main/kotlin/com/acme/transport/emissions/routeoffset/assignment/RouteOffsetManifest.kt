package com.acme.transport.emissions.routeoffset.assignment

import com.acme.transport.kit.text.normalize

/** Where a route offset manifest is in its life cycle. */
enum class RouteOffsetManifestStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a route offset manifest and its current status. */
data class RouteOffsetManifest(
    var code: String,
    var label: String,
    var routeCode: String,
    var description: String,
    var category: String,
    var timezone: String,
    var score: Double,
    var status: RouteOffsetManifestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetManifestStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetManifestStatus): Boolean {
        when (status) {
            RouteOffsetManifestStatus.OPEN -> {
                when (next) {
                    RouteOffsetManifestStatus.IN_REVIEW, RouteOffsetManifestStatus.REOPENED -> return true
                }
                return false
            }
            RouteOffsetManifestStatus.IN_REVIEW -> return next == RouteOffsetManifestStatus.REOPENED
            RouteOffsetManifestStatus.RESOLVED -> {
                when (next) {
                    RouteOffsetManifestStatus.REOPENED, RouteOffsetManifestStatus.ARCHIVED -> return true
                }
                return false
            }
            RouteOffsetManifestStatus.REOPENED -> return next == RouteOffsetManifestStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RouteOffsetManifestStatus) {
        if (!canMoveTo(next)) {
            error("route offset manifest $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = score

    fun snapshot(): RouteOffsetManifest {
        return RouteOffsetManifest(
            code = code,
            label = label,
            routeCode = routeCode,
            description = description,
            category = category,
            timezone = timezone,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a route offset manifest in status open. */
fun newRouteOffsetManifest(code: String, label: String): RouteOffsetManifest {
    return RouteOffsetManifest(
        code = code,
        label = label,
        routeCode = "",
        description = "",
        category = "",
        timezone = "",
        score = 0.0,
        status = RouteOffsetManifestStatus.OPEN,
    )
}
