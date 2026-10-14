package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a zone checkpoint is in its life cycle. */
enum class ZoneCheckpointStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A zone checkpoint as the zone team keeps it. */
data class ZoneCheckpoint(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var margin: Double,
    var approved: Boolean,
    var maximum: Double,
    var status: ZoneCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ZoneCheckpointStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ZoneCheckpointStatus): Boolean {
        when (status) {
            ZoneCheckpointStatus.IDLE -> {
                when (next) {
                    ZoneCheckpointStatus.READY -> return true
                }
                return false
            }
            ZoneCheckpointStatus.WARMING -> return next == ZoneCheckpointStatus.READY || next == ZoneCheckpointStatus.STOPPED
            ZoneCheckpointStatus.READY -> {
                when (next) {
                    ZoneCheckpointStatus.STOPPED -> return true
                }
                return false
            }
            ZoneCheckpointStatus.DRAINING -> {
                when (next) {
                    ZoneCheckpointStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ZoneCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("zone checkpoint $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"zone checkpoint"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netValue(): Double = round(margin * maximum * 100) / 100

    fun snapshot(): ZoneCheckpoint {
        return ZoneCheckpoint(
            code = code,
            label = label,
            attributes = attributes,
            margin = margin,
            approved = approved,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a zone checkpoint in status idle. */
fun newZoneCheckpoint(code: String, label: String): ZoneCheckpoint {
    return ZoneCheckpoint(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        margin = 0.0,
        approved = false,
        maximum = 0.0,
        status = ZoneCheckpointStatus.IDLE,
    )
}
