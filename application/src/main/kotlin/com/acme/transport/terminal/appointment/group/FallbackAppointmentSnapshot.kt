package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback appointment snapshot is in its life cycle. */
enum class FallbackAppointmentSnapshotStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One fallback appointment snapshot, with what the planners need to act on it. */
data class FallbackAppointmentSnapshot(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var notes: String,
    var attempts: Int,
    var title: String,
    var rate: Double,
    var revision: Int,
    var threshold: Double,
    var status: FallbackAppointmentSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackAppointmentSnapshotStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackAppointmentSnapshotStatus): Boolean {
        when (status) {
            FallbackAppointmentSnapshotStatus.NEW -> {
                when (next) {
                    FallbackAppointmentSnapshotStatus.QUEUED -> return true
                }
                return false
            }
            FallbackAppointmentSnapshotStatus.QUEUED -> return next == FallbackAppointmentSnapshotStatus.RUNNING
            FallbackAppointmentSnapshotStatus.RUNNING -> return next == FallbackAppointmentSnapshotStatus.DONE || next == FallbackAppointmentSnapshotStatus.FAILED
            FallbackAppointmentSnapshotStatus.DONE -> return next == FallbackAppointmentSnapshotStatus.FAILED
        }
        return false
    }

    fun moveTo(next: FallbackAppointmentSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("fallback appointment snapshot $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedTotal(): Double = round(rate * attempts.toDouble() * 100) / 100

    fun snapshot(): FallbackAppointmentSnapshot {
        return FallbackAppointmentSnapshot(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            notes = notes,
            attempts = attempts,
            title = title,
            rate = rate,
            revision = revision,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback appointment snapshot in status new. */
fun newFallbackAppointmentSnapshot(code: String, label: String): FallbackAppointmentSnapshot {
    return FallbackAppointmentSnapshot(
        code = code,
        label = label,
        durationMinutes = 0,
        notes = "",
        attempts = 0,
        title = "",
        rate = 0.0,
        revision = 0,
        threshold = 0.0,
        status = FallbackAppointmentSnapshotStatus.NEW,
    )
}
