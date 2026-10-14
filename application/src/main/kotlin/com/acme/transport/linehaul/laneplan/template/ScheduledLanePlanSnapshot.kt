package com.acme.transport.linehaul.laneplan.template

import com.acme.transport.kit.text.normalize

/** Where a scheduled lane plan snapshot is in its life cycle. */
enum class ScheduledLanePlanSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a scheduled lane plan snapshot and its current status. */
data class ScheduledLanePlanSnapshot(
    var code: String,
    var label: String,
    var notes: String,
    var timezone: String,
    var name: String,
    var fuelLiters: Double,
    var status: ScheduledLanePlanSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLanePlanSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLanePlanSnapshotStatus): Boolean {
        when (status) {
            ScheduledLanePlanSnapshotStatus.NEW -> return next == ScheduledLanePlanSnapshotStatus.QUEUED || next == ScheduledLanePlanSnapshotStatus.FAILED
            ScheduledLanePlanSnapshotStatus.QUEUED -> {
                when (next) {
                    ScheduledLanePlanSnapshotStatus.DONE, ScheduledLanePlanSnapshotStatus.FAILED -> return true
                }
                return false
            }
            ScheduledLanePlanSnapshotStatus.RUNNING -> return next == ScheduledLanePlanSnapshotStatus.FAILED
            ScheduledLanePlanSnapshotStatus.DONE -> {
                when (next) {
                    ScheduledLanePlanSnapshotStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledLanePlanSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("scheduled lane plan snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = fuelLiters

    fun snapshot(): ScheduledLanePlanSnapshot {
        return ScheduledLanePlanSnapshot(
            code = code,
            label = label,
            notes = notes,
            timezone = timezone,
            name = name,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled lane plan snapshot in status new. */
fun newScheduledLanePlanSnapshot(code: String, label: String): ScheduledLanePlanSnapshot {
    return ScheduledLanePlanSnapshot(
        code = code,
        label = label,
        notes = "",
        timezone = "",
        name = "",
        fuelLiters = 0.0,
        status = ScheduledLanePlanSnapshotStatus.NEW,
    )
}
