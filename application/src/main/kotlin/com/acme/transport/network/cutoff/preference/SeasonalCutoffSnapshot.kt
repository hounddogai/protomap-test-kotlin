package com.acme.transport.network.cutoff.preference

import com.acme.transport.kit.text.normalize

/** Where a seasonal cutoff snapshot is in its life cycle. */
enum class SeasonalCutoffSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a seasonal cutoff snapshot and its current status. */
data class SeasonalCutoffSnapshot(
    var code: String,
    var label: String,
    var ratio: Double,
    var approved: Boolean,
    var title: String,
    var status: SeasonalCutoffSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCutoffSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCutoffSnapshotStatus): Boolean {
        when (status) {
            SeasonalCutoffSnapshotStatus.NEW -> return next == SeasonalCutoffSnapshotStatus.DONE
            SeasonalCutoffSnapshotStatus.QUEUED -> return next == SeasonalCutoffSnapshotStatus.DONE
            SeasonalCutoffSnapshotStatus.RUNNING -> return next == SeasonalCutoffSnapshotStatus.DONE || next == SeasonalCutoffSnapshotStatus.FAILED
            SeasonalCutoffSnapshotStatus.DONE -> {
                when (next) {
                    SeasonalCutoffSnapshotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalCutoffSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("seasonal cutoff snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"seasonal cutoff snapshot"} $code"

    fun measuredValue(): Double = ratio

    fun snapshot(): SeasonalCutoffSnapshot {
        return SeasonalCutoffSnapshot(
            code = code,
            label = label,
            ratio = ratio,
            approved = approved,
            title = title,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a seasonal cutoff snapshot in status new. */
fun newSeasonalCutoffSnapshot(code: String, label: String): SeasonalCutoffSnapshot {
    return SeasonalCutoffSnapshot(
        code = code,
        label = label,
        ratio = 0.0,
        approved = false,
        title = "",
        status = SeasonalCutoffSnapshotStatus.NEW,
    )
}
