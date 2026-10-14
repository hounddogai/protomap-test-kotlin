package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled hub snapshot is in its life cycle. */
enum class ScheduledHubSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A scheduled hub snapshot as the hub team keeps it. */
data class ScheduledHubSnapshot(
    var code: String,
    var label: String,
    var cost: Double,
    var revision: Int,
    var source: String,
    var status: ScheduledHubSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledHubSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledHubSnapshotStatus): Boolean {
        when (status) {
            ScheduledHubSnapshotStatus.NEW -> return next == ScheduledHubSnapshotStatus.QUEUED || next == ScheduledHubSnapshotStatus.FAILED
            ScheduledHubSnapshotStatus.QUEUED -> return next == ScheduledHubSnapshotStatus.RUNNING || next == ScheduledHubSnapshotStatus.FAILED
            ScheduledHubSnapshotStatus.RUNNING -> return next == ScheduledHubSnapshotStatus.DONE || next == ScheduledHubSnapshotStatus.FAILED
            ScheduledHubSnapshotStatus.DONE -> {
                when (next) {
                    ScheduledHubSnapshotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledHubSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("scheduled hub snapshot $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveLoad(): Double = round(revision.toDouble() * cost * 100) / 100

    fun snapshot(): ScheduledHubSnapshot {
        return ScheduledHubSnapshot(
            code = code,
            label = label,
            cost = cost,
            revision = revision,
            source = source,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a scheduled hub snapshot in status new. */
fun newScheduledHubSnapshot(code: String, label: String): ScheduledHubSnapshot {
    return ScheduledHubSnapshot(
        code = code,
        label = label,
        cost = 0.0,
        revision = 0,
        source = "",
        status = ScheduledHubSnapshotStatus.NEW,
    )
}
