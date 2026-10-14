package com.acme.transport.emissions.fuelburn.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional fuel burn target is in its life cycle. */
enum class RegionalFuelBurnTargetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A regional fuel burn target as the fuel burn team keeps it. */
data class RegionalFuelBurnTarget(
    var code: String,
    var title: String,
    var vehicleId: String,
    var pallets: Int,
    var version: Int,
    var country: String,
    var tags: MutableList<String>,
    var trailerId: String,
    var routeCode: String,
    var windowDays: Int,
    var status: RegionalFuelBurnTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalFuelBurnTargetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalFuelBurnTargetStatus): Boolean {
        when (status) {
            RegionalFuelBurnTargetStatus.OPEN -> return next == RegionalFuelBurnTargetStatus.IN_REVIEW || next == RegionalFuelBurnTargetStatus.RESOLVED
            RegionalFuelBurnTargetStatus.IN_REVIEW -> return next == RegionalFuelBurnTargetStatus.ARCHIVED
            RegionalFuelBurnTargetStatus.RESOLVED -> return next == RegionalFuelBurnTargetStatus.REOPENED || next == RegionalFuelBurnTargetStatus.ARCHIVED
            RegionalFuelBurnTargetStatus.REOPENED -> return next == RegionalFuelBurnTargetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RegionalFuelBurnTargetStatus) {
        if (!canMoveTo(next)) {
            error("regional fuel burn target $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "${"regional fuel burn target"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedValue(): Double = round((pallets.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): RegionalFuelBurnTarget {
        return RegionalFuelBurnTarget(
            code = code,
            title = title,
            vehicleId = vehicleId,
            pallets = pallets,
            version = version,
            country = country,
            tags = tags,
            trailerId = trailerId,
            routeCode = routeCode,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a regional fuel burn target in status open. */
fun newRegionalFuelBurnTarget(code: String, title: String): RegionalFuelBurnTarget {
    return RegionalFuelBurnTarget(
        code = code,
        title = title,
        vehicleId = "",
        pallets = 0,
        version = 0,
        country = "",
        tags = mutableListOf<String>(),
        trailerId = "",
        routeCode = "",
        windowDays = 0,
        status = RegionalFuelBurnTargetStatus.OPEN,
    )
}
