package com.acme.transport.customs.clearance.primaryclearanceoverride

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict clearance assignment is in its life cycle. */
enum class StrictClearanceAssignmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a strict clearance assignment and its current status. */
data class StrictClearanceAssignment(
    var code: String,
    var label: String,
    var contactEmail: String,
    var vehicleId: String,
    var pinned: Boolean,
    var distanceKm: Double,
    var floorValue: Double,
    var limit: Int,
    var threshold: Double,
    var status: StrictClearanceAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictClearanceAssignmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictClearanceAssignmentStatus): Boolean {
        when (status) {
            StrictClearanceAssignmentStatus.NEW -> {
                when (next) {
                    StrictClearanceAssignmentStatus.QUEUED -> return true
                }
                return false
            }
            StrictClearanceAssignmentStatus.QUEUED -> return next == StrictClearanceAssignmentStatus.RUNNING || next == StrictClearanceAssignmentStatus.FAILED
            StrictClearanceAssignmentStatus.RUNNING -> {
                when (next) {
                    StrictClearanceAssignmentStatus.FAILED -> return true
                }
                return false
            }
            StrictClearanceAssignmentStatus.DONE -> {
                when (next) {
                    StrictClearanceAssignmentStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictClearanceAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("strict clearance assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveVolume(): Double = round((distanceKm + threshold) * 100) / 100

    fun snapshot(): StrictClearanceAssignment {
        return StrictClearanceAssignment(
            code = code,
            label = label,
            contactEmail = contactEmail,
            vehicleId = vehicleId,
            pinned = pinned,
            distanceKm = distanceKm,
            floorValue = floorValue,
            limit = limit,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a strict clearance assignment in status new. */
fun newStrictClearanceAssignment(code: String, label: String): StrictClearanceAssignment {
    return StrictClearanceAssignment(
        code = code,
        label = label,
        contactEmail = "",
        vehicleId = "",
        pinned = false,
        distanceKm = 0.0,
        floorValue = 0.0,
        limit = 0,
        threshold = 0.0,
        status = StrictClearanceAssignmentStatus.NEW,
    )
}
