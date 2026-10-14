package com.acme.transport.safety.scorecard.scorecardgroup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard checkpoint is in its life cycle. */
enum class ScorecardCheckpointStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A scorecard checkpoint as the scorecard team keeps it. */
data class ScorecardCheckpoint(
    var code: String,
    var name: String,
    var notes: String,
    var amount: Double,
    var channel: String,
    var ownerId: String,
    var segment: String,
    var floorValue: Double,
    var pallets: Int,
    var status: ScorecardCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardCheckpointStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardCheckpointStatus): Boolean {
        when (status) {
            ScorecardCheckpointStatus.REQUESTED -> return next == ScorecardCheckpointStatus.GRANTED || next == ScorecardCheckpointStatus.EXPIRED
            ScorecardCheckpointStatus.GRANTED -> {
                when (next) {
                    ScorecardCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
            ScorecardCheckpointStatus.REVOKED -> return next == ScorecardCheckpointStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ScorecardCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("scorecard checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveValue(): Double = round(floorValue * pallets.toDouble() * 100) / 100

    fun snapshot(): ScorecardCheckpoint {
        return ScorecardCheckpoint(
            code = code,
            name = name,
            notes = notes,
            amount = amount,
            channel = channel,
            ownerId = ownerId,
            segment = segment,
            floorValue = floorValue,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a scorecard checkpoint in status requested. */
fun newScorecardCheckpoint(code: String, name: String): ScorecardCheckpoint {
    return ScorecardCheckpoint(
        code = code,
        name = name,
        notes = "",
        amount = 0.0,
        channel = "",
        ownerId = "",
        segment = "",
        floorValue = 0.0,
        pallets = 0,
        status = ScorecardCheckpointStatus.REQUESTED,
    )
}
