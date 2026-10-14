package com.acme.transport.emissions.fuelburn.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel burn assignment is in its life cycle. */
enum class FuelBurnAssignmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a fuel burn assignment and its current status. */
data class FuelBurnAssignment(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var checksum: String,
    var odometer: Int,
    var description: String,
    var ratio: Double,
    var retries: Int,
    var status: FuelBurnAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnAssignmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnAssignmentStatus): Boolean {
        when (status) {
            FuelBurnAssignmentStatus.PLANNED -> return next == FuelBurnAssignmentStatus.SCHEDULED || next == FuelBurnAssignmentStatus.COMPLETED
            FuelBurnAssignmentStatus.SCHEDULED -> return next == FuelBurnAssignmentStatus.COMPLETED
            FuelBurnAssignmentStatus.IN_PROGRESS -> return next == FuelBurnAssignmentStatus.COMPLETED || next == FuelBurnAssignmentStatus.CANCELLED
            FuelBurnAssignmentStatus.COMPLETED -> {
                when (next) {
                    FuelBurnAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedLoad(): Double = round((retries.toDouble() - ratio) * 100) / 100

    fun snapshot(): FuelBurnAssignment {
        return FuelBurnAssignment(
            code = code,
            label = label,
            tags = tags,
            checksum = checksum,
            odometer = odometer,
            description = description,
            ratio = ratio,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fuel burn assignment in status planned. */
fun newFuelBurnAssignment(code: String, label: String): FuelBurnAssignment {
    return FuelBurnAssignment(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        checksum = "",
        odometer = 0,
        description = "",
        ratio = 0.0,
        retries = 0,
        status = FuelBurnAssignmentStatus.PLANNED,
    )
}
