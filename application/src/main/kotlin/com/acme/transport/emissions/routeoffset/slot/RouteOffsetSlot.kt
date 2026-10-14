package com.acme.transport.emissions.routeoffset.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The route offset slot records of emissions's route offset work, and the rules, stores, and calculations
// around them.

/** Where a route offset slot is in its life cycle. */
enum class RouteOffsetSlotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A route offset slot as the route offset team keeps it. */
data class RouteOffsetSlot(
    var code: String,
    var title: String,
    var contactEmail: String,
    var threshold: Double,
    var label: String,
    var capacity: Int,
    var limit: Int,
    var ratio: Double,
    var budget: Double,
    var status: RouteOffsetSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetSlotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetSlotStatus): Boolean {
        when (status) {
            RouteOffsetSlotStatus.REQUESTED -> {
                when (next) {
                    RouteOffsetSlotStatus.EXPIRED -> return true
                }
                return false
            }
            RouteOffsetSlotStatus.GRANTED -> {
                when (next) {
                    RouteOffsetSlotStatus.REVOKED, RouteOffsetSlotStatus.EXPIRED -> return true
                }
                return false
            }
            RouteOffsetSlotStatus.REVOKED -> {
                when (next) {
                    RouteOffsetSlotStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetSlotStatus) {
        if (!canMoveTo(next)) {
            error("route offset slot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"route offset slot"} $code"

    fun netTotal(): Double = round((threshold + limit.toDouble()) * 100) / 100

    fun snapshot(): RouteOffsetSlot {
        return RouteOffsetSlot(
            code = code,
            title = title,
            contactEmail = contactEmail,
            threshold = threshold,
            label = label,
            capacity = capacity,
            limit = limit,
            ratio = ratio,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a route offset slot in status requested. */
fun newRouteOffsetSlot(code: String, title: String): RouteOffsetSlot {
    return RouteOffsetSlot(
        code = code,
        title = title,
        contactEmail = "",
        threshold = 0.0,
        label = "",
        capacity = 0,
        limit = 0,
        ratio = 0.0,
        budget = 0.0,
        status = RouteOffsetSlotStatus.REQUESTED,
    )
}
