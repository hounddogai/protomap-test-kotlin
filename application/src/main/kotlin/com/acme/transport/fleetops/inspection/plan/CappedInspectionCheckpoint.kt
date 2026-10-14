package com.acme.transport.fleetops.inspection.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped inspection checkpoint is in its life cycle. */
enum class CappedInspectionCheckpointStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One capped inspection checkpoint, with what the planners need to act on it. */
data class CappedInspectionCheckpoint(
    var code: String,
    var name: String,
    var sequence: Int,
    var segment: String,
    var lane: String,
    var distanceKm: Double,
    var status: CappedInspectionCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedInspectionCheckpointStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedInspectionCheckpointStatus): Boolean {
        when (status) {
            CappedInspectionCheckpointStatus.PLANNED -> return next == CappedInspectionCheckpointStatus.SCHEDULED
            CappedInspectionCheckpointStatus.SCHEDULED -> {
                when (next) {
                    CappedInspectionCheckpointStatus.COMPLETED -> return true
                }
                return false
            }
            CappedInspectionCheckpointStatus.IN_PROGRESS -> {
                when (next) {
                    CappedInspectionCheckpointStatus.CANCELLED -> return true
                }
                return false
            }
            CappedInspectionCheckpointStatus.COMPLETED -> return next == CappedInspectionCheckpointStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: CappedInspectionCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("capped inspection checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun adjustedTotal(): Double = round((sequence.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): CappedInspectionCheckpoint {
        return CappedInspectionCheckpoint(
            code = code,
            name = name,
            sequence = sequence,
            segment = segment,
            lane = lane,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a capped inspection checkpoint in status planned. */
fun newCappedInspectionCheckpoint(code: String, name: String): CappedInspectionCheckpoint {
    return CappedInspectionCheckpoint(
        code = code,
        name = name,
        sequence = 0,
        segment = "",
        lane = "",
        distanceKm = 0.0,
        status = CappedInspectionCheckpointStatus.PLANNED,
    )
}
