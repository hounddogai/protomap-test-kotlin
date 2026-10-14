package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending appointment cycle is in its life cycle. */
enum class PendingAppointmentCycleStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A pending appointment cycle. Its code is unique within a tenant. */
data class PendingAppointmentCycle(
    var code: String,
    var name: String,
    var odometer: Int,
    var vehicleId: String,
    var durationMinutes: Int,
    var currency: String,
    var floorValue: Double,
    var revision: Int,
    var status: PendingAppointmentCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAppointmentCycleStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAppointmentCycleStatus): Boolean {
        when (status) {
            PendingAppointmentCycleStatus.CREATED -> {
                when (next) {
                    PendingAppointmentCycleStatus.PUBLISHED -> return true
                }
                return false
            }
            PendingAppointmentCycleStatus.VALIDATED -> {
                when (next) {
                    PendingAppointmentCycleStatus.PUBLISHED -> return true
                }
                return false
            }
            PendingAppointmentCycleStatus.PUBLISHED -> return next == PendingAppointmentCycleStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: PendingAppointmentCycleStatus) {
        if (!canMoveTo(next)) {
            error("pending appointment cycle $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"pending appointment cycle"} $code"

    fun netLoad(): Double = round((floorValue - revision.toDouble()) * 100) / 100

    fun snapshot(): PendingAppointmentCycle {
        return PendingAppointmentCycle(
            code = code,
            name = name,
            odometer = odometer,
            vehicleId = vehicleId,
            durationMinutes = durationMinutes,
            currency = currency,
            floorValue = floorValue,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a pending appointment cycle in status created. */
fun newPendingAppointmentCycle(code: String, name: String): PendingAppointmentCycle {
    return PendingAppointmentCycle(
        code = code,
        name = name,
        odometer = 0,
        vehicleId = "",
        durationMinutes = 0,
        currency = "",
        floorValue = 0.0,
        revision = 0,
        status = PendingAppointmentCycleStatus.CREATED,
    )
}
