package com.acme.transport.fleetops.tire.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tire snapshot is in its life cycle. */
enum class TireSnapshotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One tire snapshot, with what the planners need to act on it. */
data class TireSnapshot(
    var code: String,
    var label: String,
    var stopCount: Int,
    var maximum: Double,
    var pinned: Boolean,
    var active: Boolean,
    var phone: String,
    var approved: Boolean,
    var status: TireSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TireSnapshotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: TireSnapshotStatus): Boolean {
        when (status) {
            TireSnapshotStatus.PLANNED -> return next == TireSnapshotStatus.COMPLETED || next == TireSnapshotStatus.CANCELLED
            TireSnapshotStatus.SCHEDULED -> return next == TireSnapshotStatus.CANCELLED
            TireSnapshotStatus.IN_PROGRESS -> return next == TireSnapshotStatus.CANCELLED
            TireSnapshotStatus.COMPLETED -> return next == TireSnapshotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: TireSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("tire snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"tire snapshot"} $code"

    fun weightedExposure(): Double = round((maximum - stopCount.toDouble()) * 100) / 100

    fun snapshot(): TireSnapshot {
        return TireSnapshot(
            code = code,
            label = label,
            stopCount = stopCount,
            maximum = maximum,
            pinned = pinned,
            active = active,
            phone = phone,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a tire snapshot in status planned. */
fun newTireSnapshot(code: String, label: String): TireSnapshot {
    return TireSnapshot(
        code = code,
        label = label,
        stopCount = 0,
        maximum = 0.0,
        pinned = false,
        active = false,
        phone = "",
        approved = false,
        status = TireSnapshotStatus.PLANNED,
    )
}
