package com.acme.transport.safety.incident.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict incident snapshot is in its life cycle. */
enum class StrictIncidentSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A strict incident snapshot. Its code is unique within a tenant. */
data class StrictIncidentSnapshot(
    var code: String,
    var title: String,
    var weight: Double,
    var trailerId: String,
    var shiftCode: String,
    var threshold: Double,
    var status: StrictIncidentSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictIncidentSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictIncidentSnapshotStatus): Boolean {
        when (status) {
            StrictIncidentSnapshotStatus.NEW -> {
                when (next) {
                    StrictIncidentSnapshotStatus.RUNNING -> return true
                }
                return false
            }
            StrictIncidentSnapshotStatus.QUEUED -> {
                when (next) {
                    StrictIncidentSnapshotStatus.DONE -> return true
                }
                return false
            }
            StrictIncidentSnapshotStatus.RUNNING -> {
                when (next) {
                    StrictIncidentSnapshotStatus.DONE -> return true
                }
                return false
            }
            StrictIncidentSnapshotStatus.DONE -> return next == StrictIncidentSnapshotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: StrictIncidentSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("strict incident snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict incident snapshot"} $code"

    fun netTotal(): Double = round((threshold + weight) * 100) / 100

    fun snapshot(): StrictIncidentSnapshot {
        return StrictIncidentSnapshot(
            code = code,
            title = title,
            weight = weight,
            trailerId = trailerId,
            shiftCode = shiftCode,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict incident snapshot in status new. */
fun newStrictIncidentSnapshot(code: String, title: String): StrictIncidentSnapshot {
    return StrictIncidentSnapshot(
        code = code,
        title = title,
        weight = 0.0,
        trailerId = "",
        shiftCode = "",
        threshold = 0.0,
        status = StrictIncidentSnapshotStatus.NEW,
    )
}
