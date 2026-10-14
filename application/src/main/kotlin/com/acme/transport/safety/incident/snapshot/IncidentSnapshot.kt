package com.acme.transport.safety.incident.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The incident snapshot records of safety's incident work, and the rules, stores, and calculations around
// them.

/** Where a incident snapshot is in its life cycle. */
enum class IncidentSnapshotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a incident snapshot and its current status. */
data class IncidentSnapshot(
    var code: String,
    var title: String,
    var tags: MutableList<String>,
    var rate: Double,
    var name: String,
    var sequence: Int,
    var durationMinutes: Int,
    var carrier: String,
    var status: IncidentSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            IncidentSnapshotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: IncidentSnapshotStatus): Boolean {
        when (status) {
            IncidentSnapshotStatus.OPEN -> {
                when (next) {
                    IncidentSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
            IncidentSnapshotStatus.IN_REVIEW -> return next == IncidentSnapshotStatus.REOPENED
            IncidentSnapshotStatus.RESOLVED -> {
                when (next) {
                    IncidentSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
            IncidentSnapshotStatus.REOPENED -> {
                when (next) {
                    IncidentSnapshotStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: IncidentSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("incident snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun weightedVolume(): Double = round(durationMinutes.toDouble() * rate * 100) / 100

    fun snapshot(): IncidentSnapshot {
        return IncidentSnapshot(
            code = code,
            title = title,
            tags = tags,
            rate = rate,
            name = name,
            sequence = sequence,
            durationMinutes = durationMinutes,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a incident snapshot in status open. */
fun newIncidentSnapshot(code: String, title: String): IncidentSnapshot {
    return IncidentSnapshot(
        code = code,
        title = title,
        tags = mutableListOf<String>(),
        rate = 0.0,
        name = "",
        sequence = 0,
        durationMinutes = 0,
        carrier = "",
        status = IncidentSnapshotStatus.OPEN,
    )
}
