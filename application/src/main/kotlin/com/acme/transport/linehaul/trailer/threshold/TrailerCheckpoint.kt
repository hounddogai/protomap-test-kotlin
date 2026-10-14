package com.acme.transport.linehaul.trailer.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a trailer checkpoint is in its life cycle. */
enum class TrailerCheckpointStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a trailer checkpoint and its current status. */
data class TrailerCheckpoint(
    var code: String,
    var label: String,
    var checksum: String,
    var budget: Double,
    var etaMinutes: Int,
    var status: TrailerCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerCheckpointStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerCheckpointStatus): Boolean {
        when (status) {
            TrailerCheckpointStatus.REQUESTED -> return next == TrailerCheckpointStatus.REVOKED || next == TrailerCheckpointStatus.EXPIRED
            TrailerCheckpointStatus.GRANTED -> return next == TrailerCheckpointStatus.EXPIRED
            TrailerCheckpointStatus.REVOKED -> {
                when (next) {
                    TrailerCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TrailerCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("trailer checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun netVolume(): Double = round((etaMinutes.toDouble() + budget) * 100) / 100

    fun snapshot(): TrailerCheckpoint {
        return TrailerCheckpoint(
            code = code,
            label = label,
            checksum = checksum,
            budget = budget,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a trailer checkpoint in status requested. */
fun newTrailerCheckpoint(code: String, label: String): TrailerCheckpoint {
    return TrailerCheckpoint(
        code = code,
        label = label,
        checksum = "",
        budget = 0.0,
        etaMinutes = 0,
        status = TrailerCheckpointStatus.REQUESTED,
    )
}
