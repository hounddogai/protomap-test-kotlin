package com.acme.transport.fleetops.inspection.inspectiongroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling inspection checkpoint is in its life cycle. */
enum class RollingInspectionCheckpointStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One rolling inspection checkpoint, with what the planners need to act on it. */
data class RollingInspectionCheckpoint(
    var code: String,
    var name: String,
    var version: Int,
    var tags: MutableList<String>,
    var batchSize: Int,
    var unit: String,
    var vehicleId: String,
    var country: String,
    var distanceKm: Double,
    var fuelLiters: Double,
    var status: RollingInspectionCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingInspectionCheckpointStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingInspectionCheckpointStatus): Boolean {
        when (status) {
            RollingInspectionCheckpointStatus.REQUESTED -> {
                when (next) {
                    RollingInspectionCheckpointStatus.GRANTED, RollingInspectionCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
            RollingInspectionCheckpointStatus.GRANTED -> {
                when (next) {
                    RollingInspectionCheckpointStatus.REVOKED, RollingInspectionCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
            RollingInspectionCheckpointStatus.REVOKED -> {
                when (next) {
                    RollingInspectionCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingInspectionCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("rolling inspection checkpoint $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code: $name"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun effectiveValue(): Double = round((batchSize.toDouble() + version.toDouble()) * 100) / 100

    fun snapshot(): RollingInspectionCheckpoint {
        return RollingInspectionCheckpoint(
            code = code,
            name = name,
            version = version,
            tags = tags,
            batchSize = batchSize,
            unit = unit,
            vehicleId = vehicleId,
            country = country,
            distanceKm = distanceKm,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a rolling inspection checkpoint in status requested. */
fun newRollingInspectionCheckpoint(code: String, name: String): RollingInspectionCheckpoint {
    return RollingInspectionCheckpoint(
        code = code,
        name = name,
        version = 0,
        tags = mutableListOf<String>(),
        batchSize = 0,
        unit = "",
        vehicleId = "",
        country = "",
        distanceKm = 0.0,
        fuelLiters = 0.0,
        status = RollingInspectionCheckpointStatus.REQUESTED,
    )
}
