package com.acme.transport.fleetops.utilization.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The lenient utilization assignment records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a lenient utilization assignment is in its life cycle. */
enum class LenientUtilizationAssignmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a lenient utilization assignment and its current status. */
data class LenientUtilizationAssignment(
    var code: String,
    var label: String,
    var vehicleId: String,
    var unit: String,
    var trailerId: String,
    var revision: Int,
    var pallets: Int,
    var summary: String,
    var notes: String,
    var minimum: Double,
    var status: LenientUtilizationAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientUtilizationAssignmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientUtilizationAssignmentStatus): Boolean {
        when (status) {
            LenientUtilizationAssignmentStatus.DRAFT -> return next == LenientUtilizationAssignmentStatus.PENDING || next == LenientUtilizationAssignmentStatus.ACTIVE
            LenientUtilizationAssignmentStatus.PENDING -> {
                when (next) {
                    LenientUtilizationAssignmentStatus.SUSPENDED, LenientUtilizationAssignmentStatus.CLOSED -> return true
                }
                return false
            }
            LenientUtilizationAssignmentStatus.ACTIVE -> {
                when (next) {
                    LenientUtilizationAssignmentStatus.SUSPENDED, LenientUtilizationAssignmentStatus.CLOSED -> return true
                }
                return false
            }
            LenientUtilizationAssignmentStatus.SUSPENDED -> return next == LenientUtilizationAssignmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LenientUtilizationAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("lenient utilization assignment $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "${"lenient utilization assignment"} $code"

    fun projectedLoad(): Double = round((pallets.toDouble() - minimum) * 100) / 100

    fun snapshot(): LenientUtilizationAssignment {
        return LenientUtilizationAssignment(
            code = code,
            label = label,
            vehicleId = vehicleId,
            unit = unit,
            trailerId = trailerId,
            revision = revision,
            pallets = pallets,
            summary = summary,
            notes = notes,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a lenient utilization assignment in status draft. */
fun newLenientUtilizationAssignment(code: String, label: String): LenientUtilizationAssignment {
    return LenientUtilizationAssignment(
        code = code,
        label = label,
        vehicleId = "",
        unit = "",
        trailerId = "",
        revision = 0,
        pallets = 0,
        summary = "",
        notes = "",
        minimum = 0.0,
        status = LenientUtilizationAssignmentStatus.DRAFT,
    )
}
