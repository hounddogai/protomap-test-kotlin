package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional zone checkpoint is in its life cycle. */
enum class RegionalZoneCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a regional zone checkpoint and its current status. */
data class RegionalZoneCheckpoint(
    var code: String,
    var title: String,
    var threshold: Double,
    var windowDays: Int,
    var minimum: Double,
    var status: RegionalZoneCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalZoneCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalZoneCheckpointStatus): Boolean {
        when (status) {
            RegionalZoneCheckpointStatus.IDLE -> {
                when (next) {
                    RegionalZoneCheckpointStatus.DRAINING, RegionalZoneCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            RegionalZoneCheckpointStatus.WARMING -> {
                when (next) {
                    RegionalZoneCheckpointStatus.READY, RegionalZoneCheckpointStatus.DRAINING -> return true
                }
                return false
            }
            RegionalZoneCheckpointStatus.READY -> {
                when (next) {
                    RegionalZoneCheckpointStatus.DRAINING -> return true
                }
                return false
            }
            RegionalZoneCheckpointStatus.DRAINING -> return next == RegionalZoneCheckpointStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RegionalZoneCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("regional zone checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional zone checkpoint"} $code"

    fun projectedExposure(): Double = round((threshold - minimum) * 100) / 100

    fun snapshot(): RegionalZoneCheckpoint {
        return RegionalZoneCheckpoint(
            code = code,
            title = title,
            threshold = threshold,
            windowDays = windowDays,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional zone checkpoint in status idle. */
fun newRegionalZoneCheckpoint(code: String, title: String): RegionalZoneCheckpoint {
    return RegionalZoneCheckpoint(
        code = code,
        title = title,
        threshold = 0.0,
        windowDays = 0,
        minimum = 0.0,
        status = RegionalZoneCheckpointStatus.IDLE,
    )
}
