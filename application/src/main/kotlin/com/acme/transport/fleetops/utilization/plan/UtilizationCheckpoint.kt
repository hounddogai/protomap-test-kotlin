package com.acme.transport.fleetops.utilization.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization checkpoint is in its life cycle. */
enum class UtilizationCheckpointStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A utilization checkpoint as the utilization team keeps it. */
data class UtilizationCheckpoint(
    var code: String,
    var title: String,
    var vehicleId: String,
    var threshold: Double,
    var pallets: Int,
    var lane: String,
    var quantity: Int,
    var status: UtilizationCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationCheckpointStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationCheckpointStatus): Boolean {
        when (status) {
            UtilizationCheckpointStatus.OPEN -> return next == UtilizationCheckpointStatus.IN_REVIEW || next == UtilizationCheckpointStatus.ARCHIVED
            UtilizationCheckpointStatus.IN_REVIEW -> {
                when (next) {
                    UtilizationCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
            UtilizationCheckpointStatus.RESOLVED -> return next == UtilizationCheckpointStatus.ARCHIVED
            UtilizationCheckpointStatus.REOPENED -> {
                when (next) {
                    UtilizationCheckpointStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("utilization checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netTotal(): Double = round(threshold * quantity.toDouble() * 100) / 100

    fun snapshot(): UtilizationCheckpoint {
        return UtilizationCheckpoint(
            code = code,
            title = title,
            vehicleId = vehicleId,
            threshold = threshold,
            pallets = pallets,
            lane = lane,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a utilization checkpoint in status open. */
fun newUtilizationCheckpoint(code: String, title: String): UtilizationCheckpoint {
    return UtilizationCheckpoint(
        code = code,
        title = title,
        vehicleId = "",
        threshold = 0.0,
        pallets = 0,
        lane = "",
        quantity = 0,
        status = UtilizationCheckpointStatus.OPEN,
    )
}
