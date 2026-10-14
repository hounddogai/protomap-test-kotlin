package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fuel burn baseline records of emissions's fuel burn work, and the rules, stores, and calculations
// around them.

/** Where a fuel burn baseline is in its life cycle. */
enum class FuelBurnBaselineStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One fuel burn baseline, with what the planners need to act on it. */
data class FuelBurnBaseline(
    var code: String,
    var name: String,
    var windowDays: Int,
    var attributes: MutableMap<String, String>,
    var floorValue: Double,
    var approved: Boolean,
    var ownerId: String,
    var rate: Double,
    var lane: String,
    var phone: String,
    var status: FuelBurnBaselineStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnBaselineStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnBaselineStatus): Boolean {
        when (status) {
            FuelBurnBaselineStatus.REQUESTED -> {
                when (next) {
                    FuelBurnBaselineStatus.REVOKED, FuelBurnBaselineStatus.EXPIRED -> return true
                }
                return false
            }
            FuelBurnBaselineStatus.GRANTED -> return next == FuelBurnBaselineStatus.REVOKED || next == FuelBurnBaselineStatus.EXPIRED
            FuelBurnBaselineStatus.REVOKED -> return next == FuelBurnBaselineStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: FuelBurnBaselineStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn baseline $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fuel burn baseline"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun effectiveVolume(): Double = round((rate + floorValue) * 100) / 100

    fun snapshot(): FuelBurnBaseline {
        return FuelBurnBaseline(
            code = code,
            name = name,
            windowDays = windowDays,
            attributes = attributes,
            floorValue = floorValue,
            approved = approved,
            ownerId = ownerId,
            rate = rate,
            lane = lane,
            phone = phone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a fuel burn baseline in status requested. */
fun newFuelBurnBaseline(code: String, name: String): FuelBurnBaseline {
    return FuelBurnBaseline(
        code = code,
        name = name,
        windowDays = 0,
        attributes = mutableMapOf<String, String>(),
        floorValue = 0.0,
        approved = false,
        ownerId = "",
        rate = 0.0,
        lane = "",
        phone = "",
        status = FuelBurnBaselineStatus.REQUESTED,
    )
}
