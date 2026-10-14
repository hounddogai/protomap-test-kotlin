package com.acme.transport.safety.cameraevent.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled camera event snapshot records of safety's camera event work, and the rules, stores, and
// calculations around them.

/** Where a scheduled camera event snapshot is in its life cycle. */
enum class ScheduledCameraEventSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One scheduled camera event snapshot, with what the planners need to act on it. */
data class ScheduledCameraEventSnapshot(
    var code: String,
    var name: String,
    var amount: Double,
    var trailerId: String,
    var pinned: Boolean,
    var priority: Int,
    var dock: String,
    var segment: String,
    var status: ScheduledCameraEventSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCameraEventSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCameraEventSnapshotStatus): Boolean {
        when (status) {
            ScheduledCameraEventSnapshotStatus.CREATED -> return next == ScheduledCameraEventSnapshotStatus.VALIDATED || next == ScheduledCameraEventSnapshotStatus.RETIRED
            ScheduledCameraEventSnapshotStatus.VALIDATED -> {
                when (next) {
                    ScheduledCameraEventSnapshotStatus.PUBLISHED -> return true
                }
                return false
            }
            ScheduledCameraEventSnapshotStatus.PUBLISHED -> {
                when (next) {
                    ScheduledCameraEventSnapshotStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledCameraEventSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("scheduled camera event snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun weightedExposure(): Double = round((amount + priority.toDouble()) * 100) / 100

    fun snapshot(): ScheduledCameraEventSnapshot {
        return ScheduledCameraEventSnapshot(
            code = code,
            name = name,
            amount = amount,
            trailerId = trailerId,
            pinned = pinned,
            priority = priority,
            dock = dock,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a scheduled camera event snapshot in status created. */
fun newScheduledCameraEventSnapshot(code: String, name: String): ScheduledCameraEventSnapshot {
    return ScheduledCameraEventSnapshot(
        code = code,
        name = name,
        amount = 0.0,
        trailerId = "",
        pinned = false,
        priority = 0,
        dock = "",
        segment = "",
        status = ScheduledCameraEventSnapshotStatus.CREATED,
    )
}
