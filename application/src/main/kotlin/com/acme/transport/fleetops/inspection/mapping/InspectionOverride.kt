package com.acme.transport.fleetops.inspection.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a inspection override is in its life cycle. */
enum class InspectionOverrideStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A inspection override as the inspection team keeps it. */
data class InspectionOverride(
    var code: String,
    var name: String,
    var version: Int,
    var windowDays: Int,
    var vehicleId: String,
    var stopCount: Int,
    var fuelLiters: Double,
    var minimum: Double,
    var distanceKm: Double,
    var description: String,
    var status: InspectionOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionOverrideStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionOverrideStatus): Boolean {
        when (status) {
            InspectionOverrideStatus.DRAFT -> {
                when (next) {
                    InspectionOverrideStatus.PENDING -> return true
                }
                return false
            }
            InspectionOverrideStatus.PENDING -> {
                when (next) {
                    InspectionOverrideStatus.SUSPENDED -> return true
                }
                return false
            }
            InspectionOverrideStatus.ACTIVE -> return next == InspectionOverrideStatus.SUSPENDED || next == InspectionOverrideStatus.CLOSED
            InspectionOverrideStatus.SUSPENDED -> {
                when (next) {
                    InspectionOverrideStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionOverrideStatus) {
        if (!canMoveTo(next)) {
            error("inspection override $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $name"

    fun effectiveVolume(): Double = round((distanceKm - version.toDouble()) * 100) / 100

    fun snapshot(): InspectionOverride {
        return InspectionOverride(
            code = code,
            name = name,
            version = version,
            windowDays = windowDays,
            vehicleId = vehicleId,
            stopCount = stopCount,
            fuelLiters = fuelLiters,
            minimum = minimum,
            distanceKm = distanceKm,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a inspection override in status draft. */
fun newInspectionOverride(code: String, name: String): InspectionOverride {
    return InspectionOverride(
        code = code,
        name = name,
        version = 0,
        windowDays = 0,
        vehicleId = "",
        stopCount = 0,
        fuelLiters = 0.0,
        minimum = 0.0,
        distanceKm = 0.0,
        description = "",
        status = InspectionOverrideStatus.DRAFT,
    )
}
