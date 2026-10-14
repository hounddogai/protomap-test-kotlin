package com.acme.transport.safety.incident.incidentsnapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted incident snapshot records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a weighted incident snapshot is in its life cycle. */
enum class WeightedIncidentSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One weighted incident snapshot, with what the planners need to act on it. */
data class WeightedIncidentSnapshot(
    var code: String,
    var label: String,
    var limit: Int,
    var dock: String,
    var pinned: Boolean,
    var distanceKm: Double,
    var reviewer: String,
    var timezone: String,
    var active: Boolean,
    var channel: String,
    var status: WeightedIncidentSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedIncidentSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedIncidentSnapshotStatus): Boolean {
        when (status) {
            WeightedIncidentSnapshotStatus.CREATED -> {
                when (next) {
                    WeightedIncidentSnapshotStatus.VALIDATED, WeightedIncidentSnapshotStatus.PUBLISHED -> return true
                }
                return false
            }
            WeightedIncidentSnapshotStatus.VALIDATED -> {
                when (next) {
                    WeightedIncidentSnapshotStatus.PUBLISHED, WeightedIncidentSnapshotStatus.RETIRED -> return true
                }
                return false
            }
            WeightedIncidentSnapshotStatus.PUBLISHED -> return next == WeightedIncidentSnapshotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: WeightedIncidentSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("weighted incident snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedVolume(): Double = round((limit.toDouble() - distanceKm) * 100) / 100

    fun snapshot(): WeightedIncidentSnapshot {
        return WeightedIncidentSnapshot(
            code = code,
            label = label,
            limit = limit,
            dock = dock,
            pinned = pinned,
            distanceKm = distanceKm,
            reviewer = reviewer,
            timezone = timezone,
            active = active,
            channel = channel,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a weighted incident snapshot in status created. */
fun newWeightedIncidentSnapshot(code: String, label: String): WeightedIncidentSnapshot {
    return WeightedIncidentSnapshot(
        code = code,
        label = label,
        limit = 0,
        dock = "",
        pinned = false,
        distanceKm = 0.0,
        reviewer = "",
        timezone = "",
        active = false,
        channel = "",
        status = WeightedIncidentSnapshotStatus.CREATED,
    )
}
