package com.acme.transport.terminal.appointment.appointmentscore

import com.acme.transport.kit.text.normalize

/** Where a rolling appointment snapshot is in its life cycle. */
enum class RollingAppointmentSnapshotStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a rolling appointment snapshot and its current status. */
data class RollingAppointmentSnapshot(
    var code: String,
    var label: String,
    var region: String,
    var unit: String,
    var channel: String,
    var tags: MutableList<String>,
    var score: Double,
    var status: RollingAppointmentSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingAppointmentSnapshotStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingAppointmentSnapshotStatus): Boolean {
        when (status) {
            RollingAppointmentSnapshotStatus.PLANNED -> return next == RollingAppointmentSnapshotStatus.IN_PROGRESS
            RollingAppointmentSnapshotStatus.SCHEDULED -> {
                when (next) {
                    RollingAppointmentSnapshotStatus.IN_PROGRESS, RollingAppointmentSnapshotStatus.CANCELLED -> return true
                }
                return false
            }
            RollingAppointmentSnapshotStatus.IN_PROGRESS -> return next == RollingAppointmentSnapshotStatus.COMPLETED || next == RollingAppointmentSnapshotStatus.CANCELLED
            RollingAppointmentSnapshotStatus.COMPLETED -> return next == RollingAppointmentSnapshotStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RollingAppointmentSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("rolling appointment snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = score

    fun snapshot(): RollingAppointmentSnapshot {
        return RollingAppointmentSnapshot(
            code = code,
            label = label,
            region = region,
            unit = unit,
            channel = channel,
            tags = tags,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a rolling appointment snapshot in status planned. */
fun newRollingAppointmentSnapshot(code: String, label: String): RollingAppointmentSnapshot {
    return RollingAppointmentSnapshot(
        code = code,
        label = label,
        region = "",
        unit = "",
        channel = "",
        tags = mutableListOf<String>(),
        score = 0.0,
        status = RollingAppointmentSnapshotStatus.PLANNED,
    )
}
