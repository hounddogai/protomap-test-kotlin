package com.acme.transport.emissions.routeoffset.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The route offset entry records of emissions's route offset work, and the rules, stores, and calculations
// around them.

/** Where a route offset entry is in its life cycle. */
enum class RouteOffsetEntryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A route offset entry. Its code is unique within a tenant. */
data class RouteOffsetEntry(
    var code: String,
    var label: String,
    var threshold: Double,
    var amount: Double,
    var vehicleId: String,
    var status: RouteOffsetEntryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetEntryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetEntryStatus): Boolean {
        when (status) {
            RouteOffsetEntryStatus.OPEN -> {
                when (next) {
                    RouteOffsetEntryStatus.IN_REVIEW -> return true
                }
                return false
            }
            RouteOffsetEntryStatus.IN_REVIEW -> return next == RouteOffsetEntryStatus.REOPENED || next == RouteOffsetEntryStatus.ARCHIVED
            RouteOffsetEntryStatus.RESOLVED -> return next == RouteOffsetEntryStatus.ARCHIVED
            RouteOffsetEntryStatus.REOPENED -> {
                when (next) {
                    RouteOffsetEntryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetEntryStatus) {
        if (!canMoveTo(next)) {
            error("route offset entry $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun netTotal(): Double = round((threshold + amount) * 100) / 100

    fun snapshot(): RouteOffsetEntry {
        return RouteOffsetEntry(
            code = code,
            label = label,
            threshold = threshold,
            amount = amount,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a route offset entry in status open. */
fun newRouteOffsetEntry(code: String, label: String): RouteOffsetEntry {
    return RouteOffsetEntry(
        code = code,
        label = label,
        threshold = 0.0,
        amount = 0.0,
        vehicleId = "",
        status = RouteOffsetEntryStatus.OPEN,
    )
}
