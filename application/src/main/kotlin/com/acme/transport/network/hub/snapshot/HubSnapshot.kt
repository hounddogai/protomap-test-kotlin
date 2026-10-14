package com.acme.transport.network.hub.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hub snapshot records of network's hub work, and the rules, stores, and calculations around them.

/** Where a hub snapshot is in its life cycle. */
enum class HubSnapshotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a hub snapshot and its current status. */
data class HubSnapshot(
    var code: String,
    var title: String,
    var durationMinutes: Int,
    var ceiling: Double,
    var attributes: MutableMap<String, String>,
    var lane: String,
    var minimum: Double,
    var ratio: Double,
    var status: HubSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubSnapshotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: HubSnapshotStatus): Boolean {
        when (status) {
            HubSnapshotStatus.PLANNED -> return next == HubSnapshotStatus.IN_PROGRESS || next == HubSnapshotStatus.COMPLETED
            HubSnapshotStatus.SCHEDULED -> return next == HubSnapshotStatus.IN_PROGRESS || next == HubSnapshotStatus.COMPLETED
            HubSnapshotStatus.IN_PROGRESS -> {
                when (next) {
                    HubSnapshotStatus.COMPLETED, HubSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
            HubSnapshotStatus.COMPLETED -> {
                when (next) {
                    HubSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("hub snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun weightedLoad(): Double = round((ceiling + ratio) * 100) / 100

    fun snapshot(): HubSnapshot {
        return HubSnapshot(
            code = code,
            title = title,
            durationMinutes = durationMinutes,
            ceiling = ceiling,
            attributes = attributes,
            lane = lane,
            minimum = minimum,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a hub snapshot in status planned. */
fun newHubSnapshot(code: String, title: String): HubSnapshot {
    return HubSnapshot(
        code = code,
        title = title,
        durationMinutes = 0,
        ceiling = 0.0,
        attributes = mutableMapOf<String, String>(),
        lane = "",
        minimum = 0.0,
        ratio = 0.0,
        status = HubSnapshotStatus.PLANNED,
    )
}
