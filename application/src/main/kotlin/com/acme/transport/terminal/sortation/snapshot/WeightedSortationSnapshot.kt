package com.acme.transport.terminal.sortation.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted sortation snapshot records of terminal's sortation work, and the rules, stores, and
// calculations around them.

/** Where a weighted sortation snapshot is in its life cycle. */
enum class WeightedSortationSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a weighted sortation snapshot and its current status. */
data class WeightedSortationSnapshot(
    var code: String,
    var name: String,
    var endsAt: Int,
    var limit: Int,
    var trailerId: String,
    var loadKg: Double,
    var summary: String,
    var status: WeightedSortationSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedSortationSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedSortationSnapshotStatus): Boolean {
        when (status) {
            WeightedSortationSnapshotStatus.IDLE -> return next == WeightedSortationSnapshotStatus.DRAINING
            WeightedSortationSnapshotStatus.WARMING -> return next == WeightedSortationSnapshotStatus.READY || next == WeightedSortationSnapshotStatus.STOPPED
            WeightedSortationSnapshotStatus.READY -> return next == WeightedSortationSnapshotStatus.DRAINING || next == WeightedSortationSnapshotStatus.STOPPED
            WeightedSortationSnapshotStatus.DRAINING -> {
                when (next) {
                    WeightedSortationSnapshotStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedSortationSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("weighted sortation snapshot $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedExposure(): Double = round((limit.toDouble() - endsAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedSortationSnapshot {
        return WeightedSortationSnapshot(
            code = code,
            name = name,
            endsAt = endsAt,
            limit = limit,
            trailerId = trailerId,
            loadKg = loadKg,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted sortation snapshot in status idle. */
fun newWeightedSortationSnapshot(code: String, name: String): WeightedSortationSnapshot {
    return WeightedSortationSnapshot(
        code = code,
        name = name,
        endsAt = 0,
        limit = 0,
        trailerId = "",
        loadKg = 0.0,
        summary = "",
        status = WeightedSortationSnapshotStatus.IDLE,
    )
}
