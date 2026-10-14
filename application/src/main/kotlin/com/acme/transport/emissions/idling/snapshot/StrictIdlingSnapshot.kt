package com.acme.transport.emissions.idling.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict idling snapshot is in its life cycle. */
enum class StrictIdlingSnapshotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A strict idling snapshot. Its code is unique within a tenant. */
data class StrictIdlingSnapshot(
    var code: String,
    var label: String,
    var lane: String,
    var score: Double,
    var attempts: Int,
    var segment: String,
    var status: StrictIdlingSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictIdlingSnapshotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictIdlingSnapshotStatus): Boolean {
        when (status) {
            StrictIdlingSnapshotStatus.OPEN -> return next == StrictIdlingSnapshotStatus.REOPENED
            StrictIdlingSnapshotStatus.IN_REVIEW -> return next == StrictIdlingSnapshotStatus.RESOLVED
            StrictIdlingSnapshotStatus.RESOLVED -> return next == StrictIdlingSnapshotStatus.REOPENED || next == StrictIdlingSnapshotStatus.ARCHIVED
            StrictIdlingSnapshotStatus.REOPENED -> return next == StrictIdlingSnapshotStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: StrictIdlingSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("strict idling snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun netExposure(): Double = round((attempts.toDouble() - score) * 100) / 100

    fun snapshot(): StrictIdlingSnapshot {
        return StrictIdlingSnapshot(
            code = code,
            label = label,
            lane = lane,
            score = score,
            attempts = attempts,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a strict idling snapshot in status open. */
fun newStrictIdlingSnapshot(code: String, label: String): StrictIdlingSnapshot {
    return StrictIdlingSnapshot(
        code = code,
        label = label,
        lane = "",
        score = 0.0,
        attempts = 0,
        segment = "",
        status = StrictIdlingSnapshotStatus.OPEN,
    )
}
