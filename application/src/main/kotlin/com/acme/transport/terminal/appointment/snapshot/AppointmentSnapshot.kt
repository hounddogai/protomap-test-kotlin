package com.acme.transport.terminal.appointment.snapshot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The appointment snapshot records of terminal's appointment work, and the rules, stores, and calculations
// around them.

/** Where a appointment snapshot is in its life cycle. */
enum class AppointmentSnapshotStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One appointment snapshot, with what the planners need to act on it. */
data class AppointmentSnapshot(
    var code: String,
    var label: String,
    var dock: String,
    var depot: String,
    var pallets: Int,
    var ratio: Double,
    var status: AppointmentSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentSnapshotStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentSnapshotStatus): Boolean {
        when (status) {
            AppointmentSnapshotStatus.IDLE -> return next == AppointmentSnapshotStatus.WARMING || next == AppointmentSnapshotStatus.STOPPED
            AppointmentSnapshotStatus.WARMING -> return next == AppointmentSnapshotStatus.READY || next == AppointmentSnapshotStatus.DRAINING
            AppointmentSnapshotStatus.READY -> {
                when (next) {
                    AppointmentSnapshotStatus.DRAINING, AppointmentSnapshotStatus.STOPPED -> return true
                }
                return false
            }
            AppointmentSnapshotStatus.DRAINING -> {
                when (next) {
                    AppointmentSnapshotStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("appointment snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"appointment snapshot"} $code"

    fun adjustedVolume(): Double = round((ratio + pallets.toDouble()) * 100) / 100

    fun snapshot(): AppointmentSnapshot {
        return AppointmentSnapshot(
            code = code,
            label = label,
            dock = dock,
            depot = depot,
            pallets = pallets,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a appointment snapshot in status idle. */
fun newAppointmentSnapshot(code: String, label: String): AppointmentSnapshot {
    return AppointmentSnapshot(
        code = code,
        label = label,
        dock = "",
        depot = "",
        pallets = 0,
        ratio = 0.0,
        status = AppointmentSnapshotStatus.IDLE,
    )
}
