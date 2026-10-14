package com.acme.transport.terminal.sortation.sortationsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy sortation snapshot records of terminal's sortation work, and the rules, stores, and calculations
// around them.

/** Where a legacy sortation snapshot is in its life cycle. */
enum class LegacySortationSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A legacy sortation snapshot. Its code is unique within a tenant. */
data class LegacySortationSnapshot(
    var code: String,
    var label: String,
    var attributes: MutableMap<String, String>,
    var channel: String,
    var threshold: Double,
    var maximum: Double,
    var timezone: String,
    var capacity: Int,
    var currency: String,
    var status: LegacySortationSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacySortationSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacySortationSnapshotStatus): Boolean {
        when (status) {
            LegacySortationSnapshotStatus.NEW -> return next == LegacySortationSnapshotStatus.RUNNING || next == LegacySortationSnapshotStatus.FAILED
            LegacySortationSnapshotStatus.QUEUED -> {
                when (next) {
                    LegacySortationSnapshotStatus.DONE -> return true
                }
                return false
            }
            LegacySortationSnapshotStatus.RUNNING -> {
                when (next) {
                    LegacySortationSnapshotStatus.DONE -> return true
                }
                return false
            }
            LegacySortationSnapshotStatus.DONE -> {
                when (next) {
                    LegacySortationSnapshotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LegacySortationSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("legacy sortation snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy sortation snapshot"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun adjustedTotal(): Double = round(threshold * capacity.toDouble() * 100) / 100

    fun snapshot(): LegacySortationSnapshot {
        return LegacySortationSnapshot(
            code = code,
            label = label,
            attributes = attributes,
            channel = channel,
            threshold = threshold,
            maximum = maximum,
            timezone = timezone,
            capacity = capacity,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a legacy sortation snapshot in status new. */
fun newLegacySortationSnapshot(code: String, label: String): LegacySortationSnapshot {
    return LegacySortationSnapshot(
        code = code,
        label = label,
        attributes = mutableMapOf<String, String>(),
        channel = "",
        threshold = 0.0,
        maximum = 0.0,
        timezone = "",
        capacity = 0,
        currency = "",
        status = LegacySortationSnapshotStatus.NEW,
    )
}
