package com.acme.transport.fleetops.inspection.reservation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending inspection checkpoint is in its life cycle. */
enum class PendingInspectionCheckpointStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a pending inspection checkpoint and its current status. */
data class PendingInspectionCheckpoint(
    var code: String,
    var title: String,
    var budget: Double,
    var visible: Boolean,
    var retries: Int,
    var notes: String,
    var attempts: Int,
    var status: PendingInspectionCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingInspectionCheckpointStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingInspectionCheckpointStatus): Boolean {
        when (status) {
            PendingInspectionCheckpointStatus.PLANNED -> {
                when (next) {
                    PendingInspectionCheckpointStatus.SCHEDULED, PendingInspectionCheckpointStatus.CANCELLED -> return true
                }
                return false
            }
            PendingInspectionCheckpointStatus.SCHEDULED -> {
                when (next) {
                    PendingInspectionCheckpointStatus.CANCELLED -> return true
                }
                return false
            }
            PendingInspectionCheckpointStatus.IN_PROGRESS -> {
                when (next) {
                    PendingInspectionCheckpointStatus.COMPLETED, PendingInspectionCheckpointStatus.CANCELLED -> return true
                }
                return false
            }
            PendingInspectionCheckpointStatus.COMPLETED -> return next == PendingInspectionCheckpointStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: PendingInspectionCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("pending inspection checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"pending inspection checkpoint"} $code"

    fun weightedLoad(): Double = round((retries.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): PendingInspectionCheckpoint {
        return PendingInspectionCheckpoint(
            code = code,
            title = title,
            budget = budget,
            visible = visible,
            retries = retries,
            notes = notes,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a pending inspection checkpoint in status planned. */
fun newPendingInspectionCheckpoint(code: String, title: String): PendingInspectionCheckpoint {
    return PendingInspectionCheckpoint(
        code = code,
        title = title,
        budget = 0.0,
        visible = false,
        retries = 0,
        notes = "",
        attempts = 0,
        status = PendingInspectionCheckpointStatus.PLANNED,
    )
}
