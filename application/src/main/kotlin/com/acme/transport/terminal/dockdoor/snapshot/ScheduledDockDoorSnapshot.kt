package com.acme.transport.terminal.dockdoor.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled dock door snapshot records of terminal's dock door work, and the rules, stores, and
// calculations around them.

/** Where a scheduled dock door snapshot is in its life cycle. */
enum class ScheduledDockDoorSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a scheduled dock door snapshot and its current status. */
data class ScheduledDockDoorSnapshot(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var updatedAt: Int,
    var limit: Int,
    var distanceKm: Double,
    var description: String,
    var durationMinutes: Int,
    var status: ScheduledDockDoorSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledDockDoorSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledDockDoorSnapshotStatus): Boolean {
        when (status) {
            ScheduledDockDoorSnapshotStatus.CREATED -> return next == ScheduledDockDoorSnapshotStatus.PUBLISHED
            ScheduledDockDoorSnapshotStatus.VALIDATED -> {
                when (next) {
                    ScheduledDockDoorSnapshotStatus.PUBLISHED, ScheduledDockDoorSnapshotStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledDockDoorSnapshotStatus.PUBLISHED -> return next == ScheduledDockDoorSnapshotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledDockDoorSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("scheduled dock door snapshot $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedExposure(): Double = round((durationMinutes.toDouble() + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): ScheduledDockDoorSnapshot {
        return ScheduledDockDoorSnapshot(
            code = code,
            title = title,
            expiresAt = expiresAt,
            updatedAt = updatedAt,
            limit = limit,
            distanceKm = distanceKm,
            description = description,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled dock door snapshot in status created. */
fun newScheduledDockDoorSnapshot(code: String, title: String): ScheduledDockDoorSnapshot {
    return ScheduledDockDoorSnapshot(
        code = code,
        title = title,
        expiresAt = 0,
        updatedAt = 0,
        limit = 0,
        distanceKm = 0.0,
        description = "",
        durationMinutes = 0,
        status = ScheduledDockDoorSnapshotStatus.CREATED,
    )
}
