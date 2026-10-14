package com.acme.transport.customs.clearance.slot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a clearance checkpoint is in its life cycle. */
enum class ClearanceCheckpointStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a clearance checkpoint and its current status. */
data class ClearanceCheckpoint(
    var code: String,
    var name: String,
    var description: String,
    var windowDays: Int,
    var margin: Double,
    var weight: Double,
    var status: ClearanceCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ClearanceCheckpointStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ClearanceCheckpointStatus): Boolean {
        when (status) {
            ClearanceCheckpointStatus.REQUESTED -> {
                when (next) {
                    ClearanceCheckpointStatus.GRANTED -> return true
                }
                return false
            }
            ClearanceCheckpointStatus.GRANTED -> return next == ClearanceCheckpointStatus.REVOKED || next == ClearanceCheckpointStatus.EXPIRED
            ClearanceCheckpointStatus.REVOKED -> return next == ClearanceCheckpointStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ClearanceCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("clearance checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedLoad(): Double = round((weight - windowDays.toDouble()) * 100) / 100

    fun snapshot(): ClearanceCheckpoint {
        return ClearanceCheckpoint(
            code = code,
            name = name,
            description = description,
            windowDays = windowDays,
            margin = margin,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a clearance checkpoint in status requested. */
fun newClearanceCheckpoint(code: String, name: String): ClearanceCheckpoint {
    return ClearanceCheckpoint(
        code = code,
        name = name,
        description = "",
        windowDays = 0,
        margin = 0.0,
        weight = 0.0,
        status = ClearanceCheckpointStatus.REQUESTED,
    )
}
