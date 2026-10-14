package com.acme.transport.fleetops.defect.profile

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a defect assignment is in its life cycle. */
enum class DefectAssignmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a defect assignment and its current status. */
data class DefectAssignment(
    var code: String,
    var name: String,
    var endsAt: Int,
    var region: String,
    var pallets: Int,
    var distanceKm: Double,
    var status: DefectAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectAssignmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectAssignmentStatus): Boolean {
        when (status) {
            DefectAssignmentStatus.NEW -> {
                when (next) {
                    DefectAssignmentStatus.FAILED -> return true
                }
                return false
            }
            DefectAssignmentStatus.QUEUED -> return next == DefectAssignmentStatus.DONE || next == DefectAssignmentStatus.FAILED
            DefectAssignmentStatus.RUNNING -> return next == DefectAssignmentStatus.DONE || next == DefectAssignmentStatus.FAILED
            DefectAssignmentStatus.DONE -> return next == DefectAssignmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: DefectAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("defect assignment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveVolume(): Double = round((pallets.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): DefectAssignment {
        return DefectAssignment(
            code = code,
            name = name,
            endsAt = endsAt,
            region = region,
            pallets = pallets,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a defect assignment in status new. */
fun newDefectAssignment(code: String, name: String): DefectAssignment {
    return DefectAssignment(
        code = code,
        name = name,
        endsAt = 0,
        region = "",
        pallets = 0,
        distanceKm = 0.0,
        status = DefectAssignmentStatus.NEW,
    )
}
