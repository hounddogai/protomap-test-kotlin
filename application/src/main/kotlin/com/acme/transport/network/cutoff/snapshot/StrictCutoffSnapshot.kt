package com.acme.transport.network.cutoff.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict cutoff snapshot records of network's cutoff work, and the rules, stores, and calculations around
// them.

/** Where a strict cutoff snapshot is in its life cycle. */
enum class StrictCutoffSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a strict cutoff snapshot and its current status. */
data class StrictCutoffSnapshot(
    var code: String,
    var name: String,
    var pallets: Int,
    var locale: String,
    var capacity: Int,
    var category: String,
    var stopCount: Int,
    var status: StrictCutoffSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictCutoffSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictCutoffSnapshotStatus): Boolean {
        when (status) {
            StrictCutoffSnapshotStatus.NEW -> return next == StrictCutoffSnapshotStatus.RUNNING || next == StrictCutoffSnapshotStatus.DONE
            StrictCutoffSnapshotStatus.QUEUED -> {
                when (next) {
                    StrictCutoffSnapshotStatus.RUNNING -> return true
                }
                return false
            }
            StrictCutoffSnapshotStatus.RUNNING -> {
                when (next) {
                    StrictCutoffSnapshotStatus.DONE -> return true
                }
                return false
            }
            StrictCutoffSnapshotStatus.DONE -> {
                when (next) {
                    StrictCutoffSnapshotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictCutoffSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("strict cutoff snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"strict cutoff snapshot"} $code"

    fun weightedValue(): Double = round(stopCount.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): StrictCutoffSnapshot {
        return StrictCutoffSnapshot(
            code = code,
            name = name,
            pallets = pallets,
            locale = locale,
            capacity = capacity,
            category = category,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a strict cutoff snapshot in status new. */
fun newStrictCutoffSnapshot(code: String, name: String): StrictCutoffSnapshot {
    return StrictCutoffSnapshot(
        code = code,
        name = name,
        pallets = 0,
        locale = "",
        capacity = 0,
        category = "",
        stopCount = 0,
        status = StrictCutoffSnapshotStatus.NEW,
    )
}
