package com.acme.transport.network.transittime.overrideitem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The transit time override records of network's transit time work, and the rules, stores, and calculations
// around them.

/** Where a transit time override is in its life cycle. */
enum class TransitTimeOverrideStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A transit time override as the transit time team keeps it. */
data class TransitTimeOverride(
    var code: String,
    var name: String,
    var distanceKm: Double,
    var limit: Int,
    var category: String,
    var floorValue: Double,
    var approved: Boolean,
    var odometer: Int,
    var score: Double,
    var reviewer: String,
    var cost: Double,
    var status: TransitTimeOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeOverrideStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeOverrideStatus): Boolean {
        when (status) {
            TransitTimeOverrideStatus.IDLE -> {
                when (next) {
                    TransitTimeOverrideStatus.DRAINING -> return true
                }
                return false
            }
            TransitTimeOverrideStatus.WARMING -> {
                when (next) {
                    TransitTimeOverrideStatus.DRAINING -> return true
                }
                return false
            }
            TransitTimeOverrideStatus.READY -> return next == TransitTimeOverrideStatus.STOPPED
            TransitTimeOverrideStatus.DRAINING -> {
                when (next) {
                    TransitTimeOverrideStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TransitTimeOverrideStatus) {
        if (!canMoveTo(next)) {
            error("transit time override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedLoad(): Double = round(floorValue * limit.toDouble() * 100) / 100

    fun snapshot(): TransitTimeOverride {
        return TransitTimeOverride(
            code = code,
            name = name,
            distanceKm = distanceKm,
            limit = limit,
            category = category,
            floorValue = floorValue,
            approved = approved,
            odometer = odometer,
            score = score,
            reviewer = reviewer,
            cost = cost,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a transit time override in status idle. */
fun newTransitTimeOverride(code: String, name: String): TransitTimeOverride {
    return TransitTimeOverride(
        code = code,
        name = name,
        distanceKm = 0.0,
        limit = 0,
        category = "",
        floorValue = 0.0,
        approved = false,
        odometer = 0,
        score = 0.0,
        reviewer = "",
        cost = 0.0,
        status = TransitTimeOverrideStatus.IDLE,
    )
}
