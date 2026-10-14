package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive route offset profile records of emissions's route offset work, and the rules, stores, and
// calculations around them.

/** Where a adaptive route offset profile is in its life cycle. */
enum class AdaptiveRouteOffsetProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A adaptive route offset profile as the route offset team keeps it. */
data class AdaptiveRouteOffsetProfile(
    var code: String,
    var title: String,
    var vehicleId: String,
    var cost: Double,
    var carrier: String,
    var distanceKm: Double,
    var maximum: Double,
    var status: AdaptiveRouteOffsetProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveRouteOffsetProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveRouteOffsetProfileStatus): Boolean {
        when (status) {
            AdaptiveRouteOffsetProfileStatus.DRAFT -> return next == AdaptiveRouteOffsetProfileStatus.PENDING || next == AdaptiveRouteOffsetProfileStatus.SUSPENDED
            AdaptiveRouteOffsetProfileStatus.PENDING -> return next == AdaptiveRouteOffsetProfileStatus.ACTIVE || next == AdaptiveRouteOffsetProfileStatus.CLOSED
            AdaptiveRouteOffsetProfileStatus.ACTIVE -> {
                when (next) {
                    AdaptiveRouteOffsetProfileStatus.SUSPENDED, AdaptiveRouteOffsetProfileStatus.CLOSED -> return true
                }
                return false
            }
            AdaptiveRouteOffsetProfileStatus.SUSPENDED -> {
                when (next) {
                    AdaptiveRouteOffsetProfileStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveRouteOffsetProfileStatus) {
        if (!canMoveTo(next)) {
            error("adaptive route offset profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun effectiveTotal(): Double = round((cost - distanceKm) * 100) / 100

    fun snapshot(): AdaptiveRouteOffsetProfile {
        return AdaptiveRouteOffsetProfile(
            code = code,
            title = title,
            vehicleId = vehicleId,
            cost = cost,
            carrier = carrier,
            distanceKm = distanceKm,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive route offset profile in status draft. */
fun newAdaptiveRouteOffsetProfile(code: String, title: String): AdaptiveRouteOffsetProfile {
    return AdaptiveRouteOffsetProfile(
        code = code,
        title = title,
        vehicleId = "",
        cost = 0.0,
        carrier = "",
        distanceKm = 0.0,
        maximum = 0.0,
        status = AdaptiveRouteOffsetProfileStatus.DRAFT,
    )
}
