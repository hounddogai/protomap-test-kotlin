package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment checkpoint is in its life cycle. */
enum class AppointmentCheckpointStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A appointment checkpoint. Its code is unique within a tenant. */
data class AppointmentCheckpoint(
    var code: String,
    var name: String,
    var minimum: Double,
    var revision: Int,
    var loadKg: Double,
    var status: AppointmentCheckpointStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentCheckpointStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentCheckpointStatus): Boolean {
        when (status) {
            AppointmentCheckpointStatus.CREATED -> {
                when (next) {
                    AppointmentCheckpointStatus.VALIDATED -> return true
                }
                return false
            }
            AppointmentCheckpointStatus.VALIDATED -> {
                when (next) {
                    AppointmentCheckpointStatus.PUBLISHED, AppointmentCheckpointStatus.RETIRED -> return true
                }
                return false
            }
            AppointmentCheckpointStatus.PUBLISHED -> {
                when (next) {
                    AppointmentCheckpointStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentCheckpointStatus) {
        if (!canMoveTo(next)) {
            error("appointment checkpoint $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun weightedExposure(): Double = round((loadKg - minimum) * 100) / 100

    fun snapshot(): AppointmentCheckpoint {
        return AppointmentCheckpoint(
            code = code,
            name = name,
            minimum = minimum,
            revision = revision,
            loadKg = loadKg,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a appointment checkpoint in status created. */
fun newAppointmentCheckpoint(code: String, name: String): AppointmentCheckpoint {
    return AppointmentCheckpoint(
        code = code,
        name = name,
        minimum = 0.0,
        revision = 0,
        loadKg = 0.0,
        status = AppointmentCheckpointStatus.CREATED,
    )
}
