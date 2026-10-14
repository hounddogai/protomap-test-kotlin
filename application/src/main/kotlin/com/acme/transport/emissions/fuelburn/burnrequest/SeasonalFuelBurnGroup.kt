package com.acme.transport.emissions.fuelburn.burnrequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal fuel burn group is in its life cycle. */
enum class SeasonalFuelBurnGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A seasonal fuel burn group as the fuel burn team keeps it. */
data class SeasonalFuelBurnGroup(
    var code: String,
    var name: String,
    var version: Int,
    var externalRef: String,
    var summary: String,
    var routeCode: String,
    var weight: Double,
    var distanceKm: Double,
    var phone: String,
    var maximum: Double,
    var status: SeasonalFuelBurnGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalFuelBurnGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalFuelBurnGroupStatus): Boolean {
        when (status) {
            SeasonalFuelBurnGroupStatus.PLANNED -> return next == SeasonalFuelBurnGroupStatus.SCHEDULED
            SeasonalFuelBurnGroupStatus.SCHEDULED -> {
                when (next) {
                    SeasonalFuelBurnGroupStatus.COMPLETED -> return true
                }
                return false
            }
            SeasonalFuelBurnGroupStatus.IN_PROGRESS -> {
                when (next) {
                    SeasonalFuelBurnGroupStatus.COMPLETED, SeasonalFuelBurnGroupStatus.CANCELLED -> return true
                }
                return false
            }
            SeasonalFuelBurnGroupStatus.COMPLETED -> return next == SeasonalFuelBurnGroupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SeasonalFuelBurnGroupStatus) {
        if (!canMoveTo(next)) {
            error("seasonal fuel burn group $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun netValue(): Double = round(version.toDouble() * maximum * 100) / 100

    fun snapshot(): SeasonalFuelBurnGroup {
        return SeasonalFuelBurnGroup(
            code = code,
            name = name,
            version = version,
            externalRef = externalRef,
            summary = summary,
            routeCode = routeCode,
            weight = weight,
            distanceKm = distanceKm,
            phone = phone,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal fuel burn group in status planned. */
fun newSeasonalFuelBurnGroup(code: String, name: String): SeasonalFuelBurnGroup {
    return SeasonalFuelBurnGroup(
        code = code,
        name = name,
        version = 0,
        externalRef = "",
        summary = "",
        routeCode = "",
        weight = 0.0,
        distanceKm = 0.0,
        phone = "",
        maximum = 0.0,
        status = SeasonalFuelBurnGroupStatus.PLANNED,
    )
}
