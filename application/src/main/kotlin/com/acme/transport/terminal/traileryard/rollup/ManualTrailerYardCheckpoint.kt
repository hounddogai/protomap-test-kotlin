package com.acme.transport.terminal.traileryard.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual trailer yard checkpoint is in its life cycle. */
enum class ManualTrailerYardCheckpointStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a manual trailer yard checkpoint and its current status. */
data class ManualTrailerYardCheckpoint(
    var code: String,
    var label: String,
    var routeCode: String,
    var vehicleId: String,
    var reviewer: String,
    var pallets: Int,
    var capacity: Int,
    var ownerId: String,
    var name: String,
    var status: ManualTrailerYardCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualTrailerYardCheckpointStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualTrailerYardCheckpointStatus): Boolean {
        when (status) {
            ManualTrailerYardCheckpointStatus.DRAFT -> {
                when (next) {
                    ManualTrailerYardCheckpointStatus.ACTIVE -> return true
                }
                return false
            }
            ManualTrailerYardCheckpointStatus.PENDING -> {
                when (next) {
                    ManualTrailerYardCheckpointStatus.CLOSED -> return true
                }
                return false
            }
            ManualTrailerYardCheckpointStatus.ACTIVE -> return next == ManualTrailerYardCheckpointStatus.CLOSED
            ManualTrailerYardCheckpointStatus.SUSPENDED -> {
                when (next) {
                    ManualTrailerYardCheckpointStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualTrailerYardCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("manual trailer yard checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedValue(): Double = round(pallets.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): ManualTrailerYardCheckpoint {
        return ManualTrailerYardCheckpoint(
            code = code,
            label = label,
            routeCode = routeCode,
            vehicleId = vehicleId,
            reviewer = reviewer,
            pallets = pallets,
            capacity = capacity,
            ownerId = ownerId,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a manual trailer yard checkpoint in status draft. */
fun newManualTrailerYardCheckpoint(code: String, label: String): ManualTrailerYardCheckpoint {
    return ManualTrailerYardCheckpoint(
        code = code,
        label = label,
        routeCode = "",
        vehicleId = "",
        reviewer = "",
        pallets = 0,
        capacity = 0,
        ownerId = "",
        name = "",
        status = ManualTrailerYardCheckpointStatus.DRAFT,
    )
}
