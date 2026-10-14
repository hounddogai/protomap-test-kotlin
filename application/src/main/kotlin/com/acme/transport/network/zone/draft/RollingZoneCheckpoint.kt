package com.acme.transport.network.zone.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling zone checkpoint is in its life cycle. */
enum class RollingZoneCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a rolling zone checkpoint and its current status. */
data class RollingZoneCheckpoint(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var visible: Boolean,
    var approved: Boolean,
    var updatedAt: Int,
    var version: Int,
    var status: RollingZoneCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingZoneCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingZoneCheckpointStatus): Boolean {
        when (status) {
            RollingZoneCheckpointStatus.IDLE -> return next == RollingZoneCheckpointStatus.READY || next == RollingZoneCheckpointStatus.DRAINING
            RollingZoneCheckpointStatus.WARMING -> return next == RollingZoneCheckpointStatus.DRAINING
            RollingZoneCheckpointStatus.READY -> {
                when (next) {
                    RollingZoneCheckpointStatus.DRAINING, RollingZoneCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            RollingZoneCheckpointStatus.DRAINING -> {
                when (next) {
                    RollingZoneCheckpointStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RollingZoneCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("rolling zone checkpoint $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code [$title]"

    fun netVolume(): Double = round(version.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): RollingZoneCheckpoint {
        return RollingZoneCheckpoint(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            visible = visible,
            approved = approved,
            updatedAt = updatedAt,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling zone checkpoint in status idle. */
fun newRollingZoneCheckpoint(code: String, title: String): RollingZoneCheckpoint {
    return RollingZoneCheckpoint(
        code = code,
        title = title,
        durationMinutes = 0,
        visible = false,
        approved = false,
        updatedAt = 0,
        version = 0,
        status = RollingZoneCheckpointStatus.IDLE,
    )
}
