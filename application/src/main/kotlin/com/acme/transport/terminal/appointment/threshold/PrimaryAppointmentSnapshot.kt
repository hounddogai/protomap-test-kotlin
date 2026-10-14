package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary appointment snapshot is in its life cycle. */
enum class PrimaryAppointmentSnapshotStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One primary appointment snapshot, with what the planners need to act on it. */
data class PrimaryAppointmentSnapshot(
    var code: String,
    var label: String,
    var etaMinutes: Int,
    var margin: Double,
    var contactEmail: String,
    var weight: Double,
    var description: String,
    var status: PrimaryAppointmentSnapshotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryAppointmentSnapshotStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryAppointmentSnapshotStatus): Boolean {
        when (status) {
            PrimaryAppointmentSnapshotStatus.CREATED -> {
                when (next) {
                    PrimaryAppointmentSnapshotStatus.VALIDATED, PrimaryAppointmentSnapshotStatus.RETIRED -> return true
                }
                return false
            }
            PrimaryAppointmentSnapshotStatus.VALIDATED -> return next == PrimaryAppointmentSnapshotStatus.PUBLISHED
            PrimaryAppointmentSnapshotStatus.PUBLISHED -> return next == PrimaryAppointmentSnapshotStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PrimaryAppointmentSnapshotStatus) {
        if (!canMoveTo(next)) {
            error("primary appointment snapshot $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedVolume(): Double = round((margin + etaMinutes.toDouble()) * 100) / 100

    fun snapshot(): PrimaryAppointmentSnapshot {
        return PrimaryAppointmentSnapshot(
            code = code,
            label = label,
            etaMinutes = etaMinutes,
            margin = margin,
            contactEmail = contactEmail,
            weight = weight,
            description = description,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a primary appointment snapshot in status created. */
fun newPrimaryAppointmentSnapshot(code: String, label: String): PrimaryAppointmentSnapshot {
    return PrimaryAppointmentSnapshot(
        code = code,
        label = label,
        etaMinutes = 0,
        margin = 0.0,
        contactEmail = "",
        weight = 0.0,
        description = "",
        status = PrimaryAppointmentSnapshotStatus.CREATED,
    )
}
