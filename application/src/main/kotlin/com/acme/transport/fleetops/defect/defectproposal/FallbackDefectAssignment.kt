package com.acme.transport.fleetops.defect.defectproposal

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback defect assignment is in its life cycle. */
enum class FallbackDefectAssignmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a fallback defect assignment and its current status. */
data class FallbackDefectAssignment(
    var code: String,
    var name: String,
    var notes: String,
    var fuelLiters: Double,
    var lane: String,
    var locale: String,
    var trailerId: String,
    var archived: Boolean,
    var retries: Int,
    var endsAt: Int,
    var status: FallbackDefectAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackDefectAssignmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackDefectAssignmentStatus): Boolean {
        when (status) {
            FallbackDefectAssignmentStatus.PLANNED -> return next == FallbackDefectAssignmentStatus.IN_PROGRESS || next == FallbackDefectAssignmentStatus.COMPLETED
            FallbackDefectAssignmentStatus.SCHEDULED -> return next == FallbackDefectAssignmentStatus.CANCELLED
            FallbackDefectAssignmentStatus.IN_PROGRESS -> {
                when (next) {
                    FallbackDefectAssignmentStatus.COMPLETED, FallbackDefectAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
            FallbackDefectAssignmentStatus.COMPLETED -> {
                when (next) {
                    FallbackDefectAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackDefectAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("fallback defect assignment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"fallback defect assignment"} $code"

    fun netTotal(): Double = round(retries.toDouble() * fuelLiters * 100) / 100

    fun snapshot(): FallbackDefectAssignment {
        return FallbackDefectAssignment(
            code = code,
            name = name,
            notes = notes,
            fuelLiters = fuelLiters,
            lane = lane,
            locale = locale,
            trailerId = trailerId,
            archived = archived,
            retries = retries,
            endsAt = endsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a fallback defect assignment in status planned. */
fun newFallbackDefectAssignment(code: String, name: String): FallbackDefectAssignment {
    return FallbackDefectAssignment(
        code = code,
        name = name,
        notes = "",
        fuelLiters = 0.0,
        lane = "",
        locale = "",
        trailerId = "",
        archived = false,
        retries = 0,
        endsAt = 0,
        status = FallbackDefectAssignmentStatus.PLANNED,
    )
}
