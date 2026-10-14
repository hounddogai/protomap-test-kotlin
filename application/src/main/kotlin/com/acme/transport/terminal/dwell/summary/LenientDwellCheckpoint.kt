package com.acme.transport.terminal.dwell.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient dwell checkpoint is in its life cycle. */
enum class LenientDwellCheckpointStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A lenient dwell checkpoint. Its code is unique within a tenant. */
data class LenientDwellCheckpoint(
    var code: String,
    var label: String,
    var attempts: Int,
    var reviewer: String,
    var pinned: Boolean,
    var ratio: Double,
    var status: LenientDwellCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientDwellCheckpointStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientDwellCheckpointStatus): Boolean {
        when (status) {
            LenientDwellCheckpointStatus.REQUESTED -> return next == LenientDwellCheckpointStatus.EXPIRED
            LenientDwellCheckpointStatus.GRANTED -> {
                when (next) {
                    LenientDwellCheckpointStatus.EXPIRED -> return true
                }
                return false
            }
            LenientDwellCheckpointStatus.REVOKED -> return next == LenientDwellCheckpointStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: LenientDwellCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("lenient dwell checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netVolume(): Double = round(attempts.toDouble() * ratio * 100) / 100

    fun snapshot(): LenientDwellCheckpoint {
        return LenientDwellCheckpoint(
            code = code,
            label = label,
            attempts = attempts,
            reviewer = reviewer,
            pinned = pinned,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a lenient dwell checkpoint in status requested. */
fun newLenientDwellCheckpoint(code: String, label: String): LenientDwellCheckpoint {
    return LenientDwellCheckpoint(
        code = code,
        label = label,
        attempts = 0,
        reviewer = "",
        pinned = false,
        ratio = 0.0,
        status = LenientDwellCheckpointStatus.REQUESTED,
    )
}
