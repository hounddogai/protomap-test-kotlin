package com.acme.transport.fleetops.defect.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The defect checkpoint records of fleetops's defect work, and the rules, stores, and calculations around
// them.

/** Where a defect checkpoint is in its life cycle. */
enum class DefectCheckpointStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a defect checkpoint and its current status. */
data class DefectCheckpoint(
    var code: String,
    var name: String,
    var limit: Int,
    var driverId: String,
    var createdAt: Int,
    var startsAt: Int,
    var currency: String,
    var sequence: Int,
    var graceDays: Int,
    var reviewer: String,
    var status: DefectCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefectCheckpointStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: DefectCheckpointStatus): Boolean {
        when (status) {
            DefectCheckpointStatus.PROPOSED -> {
                when (next) {
                    DefectCheckpointStatus.APPROVED, DefectCheckpointStatus.REJECTED -> return true
                }
                return false
            }
            DefectCheckpointStatus.APPROVED -> {
                when (next) {
                    DefectCheckpointStatus.REJECTED -> return true
                }
                return false
            }
            DefectCheckpointStatus.REJECTED -> {
                when (next) {
                    DefectCheckpointStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefectCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("defect checkpoint $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun weightedTotal(): Double = round(sequence.toDouble() * graceDays.toDouble() * 100) / 100

    fun snapshot(): DefectCheckpoint {
        return DefectCheckpoint(
            code = code,
            name = name,
            limit = limit,
            driverId = driverId,
            createdAt = createdAt,
            startsAt = startsAt,
            currency = currency,
            sequence = sequence,
            graceDays = graceDays,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a defect checkpoint in status proposed. */
fun newDefectCheckpoint(code: String, name: String): DefectCheckpoint {
    return DefectCheckpoint(
        code = code,
        name = name,
        limit = 0,
        driverId = "",
        createdAt = 0,
        startsAt = 0,
        currency = "",
        sequence = 0,
        graceDays = 0,
        reviewer = "",
        status = DefectCheckpointStatus.PROPOSED,
    )
}
