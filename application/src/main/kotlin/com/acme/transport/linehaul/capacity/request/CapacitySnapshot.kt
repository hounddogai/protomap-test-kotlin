package com.acme.transport.linehaul.capacity.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capacity snapshot is in its life cycle. */
enum class CapacitySnapshotStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A capacity snapshot as the capacity team keeps it. */
data class CapacitySnapshot(
    var code: String,
    var label: String,
    var trailerId: String,
    var active: Boolean,
    var maximum: Double,
    var priority: Int,
    var startsAt: Int,
    var capacity: Int,
    var visible: Boolean,
    var attributes: MutableMap<String, String>,
    var attempts: Int,
    var status: CapacitySnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacitySnapshotStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacitySnapshotStatus): Boolean {
        when (status) {
            CapacitySnapshotStatus.REQUESTED -> {
                when (next) {
                    CapacitySnapshotStatus.EXPIRED -> return true
                }
                return false
            }
            CapacitySnapshotStatus.GRANTED -> {
                when (next) {
                    CapacitySnapshotStatus.REVOKED, CapacitySnapshotStatus.EXPIRED -> return true
                }
                return false
            }
            CapacitySnapshotStatus.REVOKED -> {
                when (next) {
                    CapacitySnapshotStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CapacitySnapshotStatus) {
        if (!canMoveTo(next)) {
            error("capacity snapshot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedValue(): Double = round((maximum + startsAt.toDouble()) * 100) / 100

    fun snapshot(): CapacitySnapshot {
        return CapacitySnapshot(
            code = code,
            label = label,
            trailerId = trailerId,
            active = active,
            maximum = maximum,
            priority = priority,
            startsAt = startsAt,
            capacity = capacity,
            visible = visible,
            attributes = attributes,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capacity snapshot in status requested. */
fun newCapacitySnapshot(code: String, label: String): CapacitySnapshot {
    return CapacitySnapshot(
        code = code,
        label = label,
        trailerId = "",
        active = false,
        maximum = 0.0,
        priority = 0,
        startsAt = 0,
        capacity = 0,
        visible = false,
        attributes = mutableMapOf<String, String>(),
        attempts = 0,
        status = CapacitySnapshotStatus.REQUESTED,
    )
}
