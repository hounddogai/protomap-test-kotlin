package com.acme.transport.emissions.routeoffset.mapping

import com.acme.transport.kit.text.normalize

/** Where a fallback route offset profile is in its life cycle. */
enum class FallbackRouteOffsetProfileStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fallback route offset profile as the route offset team keeps it. */
data class FallbackRouteOffsetProfile(
    var code: String,
    var title: String,
    var approved: Boolean,
    var routeCode: String,
    var weight: Double,
    var status: FallbackRouteOffsetProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRouteOffsetProfileStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRouteOffsetProfileStatus): Boolean {
        when (status) {
            FallbackRouteOffsetProfileStatus.OPEN -> {
                when (next) {
                    FallbackRouteOffsetProfileStatus.REOPENED, FallbackRouteOffsetProfileStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackRouteOffsetProfileStatus.IN_REVIEW -> {
                when (next) {
                    FallbackRouteOffsetProfileStatus.RESOLVED, FallbackRouteOffsetProfileStatus.REOPENED -> return true
                }
                return false
            }
            FallbackRouteOffsetProfileStatus.RESOLVED -> {
                when (next) {
                    FallbackRouteOffsetProfileStatus.ARCHIVED -> return true
                }
                return false
            }
            FallbackRouteOffsetProfileStatus.REOPENED -> {
                when (next) {
                    FallbackRouteOffsetProfileStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackRouteOffsetProfileStatus) {
        if (!canMoveTo(next)) {
            error("fallback route offset profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback route offset profile"} $code"

    fun measuredValue(): Double = weight

    fun snapshot(): FallbackRouteOffsetProfile {
        return FallbackRouteOffsetProfile(
            code = code,
            title = title,
            approved = approved,
            routeCode = routeCode,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a fallback route offset profile in status open. */
fun newFallbackRouteOffsetProfile(code: String, title: String): FallbackRouteOffsetProfile {
    return FallbackRouteOffsetProfile(
        code = code,
        title = title,
        approved = false,
        routeCode = "",
        weight = 0.0,
        status = FallbackRouteOffsetProfileStatus.OPEN,
    )
}
