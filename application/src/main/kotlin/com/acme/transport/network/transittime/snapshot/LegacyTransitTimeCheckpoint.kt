package com.acme.transport.network.transittime.snapshot

import com.acme.transport.kit.text.normalize

/** Where a legacy transit time checkpoint is in its life cycle. */
enum class LegacyTransitTimeCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A legacy transit time checkpoint. Its code is unique within a tenant. */
data class LegacyTransitTimeCheckpoint(
    var code: String,
    var label: String,
    var driverId: String,
    var shiftCode: String,
    var reviewer: String,
    var score: Double,
    var status: LegacyTransitTimeCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyTransitTimeCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyTransitTimeCheckpointStatus): Boolean {
        when (status) {
            LegacyTransitTimeCheckpointStatus.IDLE -> {
                when (next) {
                    LegacyTransitTimeCheckpointStatus.READY -> return true
                }
                return false
            }
            LegacyTransitTimeCheckpointStatus.WARMING -> return next == LegacyTransitTimeCheckpointStatus.READY || next == LegacyTransitTimeCheckpointStatus.STOPPED
            LegacyTransitTimeCheckpointStatus.READY -> return next == LegacyTransitTimeCheckpointStatus.DRAINING || next == LegacyTransitTimeCheckpointStatus.STOPPED
            LegacyTransitTimeCheckpointStatus.DRAINING -> {
                when (next) {
                    LegacyTransitTimeCheckpointStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacyTransitTimeCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("legacy transit time checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = score

    fun snapshot(): LegacyTransitTimeCheckpoint {
        return LegacyTransitTimeCheckpoint(
            code = code,
            label = label,
            driverId = driverId,
            shiftCode = shiftCode,
            reviewer = reviewer,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy transit time checkpoint in status idle. */
fun newLegacyTransitTimeCheckpoint(code: String, label: String): LegacyTransitTimeCheckpoint {
    return LegacyTransitTimeCheckpoint(
        code = code,
        label = label,
        driverId = "",
        shiftCode = "",
        reviewer = "",
        score = 0.0,
        status = LegacyTransitTimeCheckpointStatus.IDLE,
    )
}
