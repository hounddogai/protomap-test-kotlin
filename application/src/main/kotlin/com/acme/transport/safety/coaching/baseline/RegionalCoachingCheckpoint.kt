package com.acme.transport.safety.coaching.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional coaching checkpoint is in its life cycle. */
enum class RegionalCoachingCheckpointStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a regional coaching checkpoint and its current status. */
data class RegionalCoachingCheckpoint(
    var code: String,
    var label: String,
    var shiftCode: String,
    var attributes: MutableMap<String, String>,
    var active: Boolean,
    var notes: String,
    var odometer: Int,
    var fuelLiters: Double,
    var status: RegionalCoachingCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCoachingCheckpointStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCoachingCheckpointStatus): Boolean {
        when (status) {
            RegionalCoachingCheckpointStatus.OPEN -> return next == RegionalCoachingCheckpointStatus.RESOLVED || next == RegionalCoachingCheckpointStatus.ARCHIVED
            RegionalCoachingCheckpointStatus.IN_REVIEW -> return next == RegionalCoachingCheckpointStatus.ARCHIVED
            RegionalCoachingCheckpointStatus.RESOLVED -> return next == RegionalCoachingCheckpointStatus.REOPENED
            RegionalCoachingCheckpointStatus.REOPENED -> return next == RegionalCoachingCheckpointStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: RegionalCoachingCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("regional coaching checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"regional coaching checkpoint"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedVolume(): Double = round(fuelLiters * odometer.toDouble() * 100) / 100

    fun snapshot(): RegionalCoachingCheckpoint {
        return RegionalCoachingCheckpoint(
            code = code,
            label = label,
            shiftCode = shiftCode,
            attributes = attributes,
            active = active,
            notes = notes,
            odometer = odometer,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional coaching checkpoint in status open. */
fun newRegionalCoachingCheckpoint(code: String, label: String): RegionalCoachingCheckpoint {
    return RegionalCoachingCheckpoint(
        code = code,
        label = label,
        shiftCode = "",
        attributes = mutableMapOf<String, String>(),
        active = false,
        notes = "",
        odometer = 0,
        fuelLiters = 0.0,
        status = RegionalCoachingCheckpointStatus.OPEN,
    )
}
