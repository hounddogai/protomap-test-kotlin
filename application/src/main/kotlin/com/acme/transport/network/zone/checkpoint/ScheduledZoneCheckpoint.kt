package com.acme.transport.network.zone.checkpoint

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled zone checkpoint records of network's zone work, and the rules, stores, and calculations
// around them.

/** Where a scheduled zone checkpoint is in its life cycle. */
enum class ScheduledZoneCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a scheduled zone checkpoint and its current status. */
data class ScheduledZoneCheckpoint(
    var code: String,
    var name: String,
    var discount: Double,
    var distanceKm: Double,
    var routeCode: String,
    var visible: Boolean,
    var shiftCode: String,
    var status: ScheduledZoneCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledZoneCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledZoneCheckpointStatus): Boolean {
        when (status) {
            ScheduledZoneCheckpointStatus.IDLE -> {
                when (next) {
                    ScheduledZoneCheckpointStatus.READY -> return true
                }
                return false
            }
            ScheduledZoneCheckpointStatus.WARMING -> return next == ScheduledZoneCheckpointStatus.DRAINING
            ScheduledZoneCheckpointStatus.READY -> {
                when (next) {
                    ScheduledZoneCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            ScheduledZoneCheckpointStatus.DRAINING -> {
                when (next) {
                    ScheduledZoneCheckpointStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledZoneCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("scheduled zone checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled zone checkpoint"} $code"

    fun netTotal(): Double = round(distanceKm * discount * 100) / 100

    fun snapshot(): ScheduledZoneCheckpoint {
        return ScheduledZoneCheckpoint(
            code = code,
            name = name,
            discount = discount,
            distanceKm = distanceKm,
            routeCode = routeCode,
            visible = visible,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a scheduled zone checkpoint in status idle. */
fun newScheduledZoneCheckpoint(code: String, name: String): ScheduledZoneCheckpoint {
    return ScheduledZoneCheckpoint(
        code = code,
        name = name,
        discount = 0.0,
        distanceKm = 0.0,
        routeCode = "",
        visible = false,
        shiftCode = "",
        status = ScheduledZoneCheckpointStatus.IDLE,
    )
}
