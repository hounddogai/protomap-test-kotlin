package com.acme.transport.freightpay.dispute.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The dispute checkpoint records of freightpay's dispute work, and the rules, stores, and calculations around
// them.

/** Where a dispute checkpoint is in its life cycle. */
enum class DisputeCheckpointStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A dispute checkpoint. Its code is unique within a tenant. */
data class DisputeCheckpoint(
    var code: String,
    var label: String,
    var graceDays: Int,
    var score: Double,
    var driverId: String,
    var notes: String,
    var lane: String,
    var title: String,
    var etaMinutes: Int,
    var status: DisputeCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DisputeCheckpointStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: DisputeCheckpointStatus): Boolean {
        when (status) {
            DisputeCheckpointStatus.DRAFT -> {
                when (next) {
                    DisputeCheckpointStatus.ACTIVE, DisputeCheckpointStatus.CLOSED -> return true
                }
                return false
            }
            DisputeCheckpointStatus.PENDING -> return next == DisputeCheckpointStatus.CLOSED
            DisputeCheckpointStatus.ACTIVE -> return next == DisputeCheckpointStatus.SUSPENDED || next == DisputeCheckpointStatus.CLOSED
            DisputeCheckpointStatus.SUSPENDED -> {
                when (next) {
                    DisputeCheckpointStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DisputeCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("dispute checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveValue(): Double = round((score + graceDays.toDouble()) * 100) / 100

    fun snapshot(): DisputeCheckpoint {
        return DisputeCheckpoint(
            code = code,
            label = label,
            graceDays = graceDays,
            score = score,
            driverId = driverId,
            notes = notes,
            lane = lane,
            title = title,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a dispute checkpoint in status draft. */
fun newDisputeCheckpoint(code: String, label: String): DisputeCheckpoint {
    return DisputeCheckpoint(
        code = code,
        label = label,
        graceDays = 0,
        score = 0.0,
        driverId = "",
        notes = "",
        lane = "",
        title = "",
        etaMinutes = 0,
        status = DisputeCheckpointStatus.DRAFT,
    )
}
