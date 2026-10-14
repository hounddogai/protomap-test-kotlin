package com.acme.transport.terminal.appointment.appointmentmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary appointment cycle is in its life cycle. */
enum class PrimaryAppointmentCycleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a primary appointment cycle and its current status. */
data class PrimaryAppointmentCycle(
    var code: String,
    var label: String,
    var vehicleId: String,
    var revision: Int,
    var updatedAt: Int,
    var segment: String,
    var rate: Double,
    var ownerId: String,
    var status: PrimaryAppointmentCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryAppointmentCycleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryAppointmentCycleStatus): Boolean {
        when (status) {
            PrimaryAppointmentCycleStatus.IDLE -> return next == PrimaryAppointmentCycleStatus.READY
            PrimaryAppointmentCycleStatus.WARMING -> {
                when (next) {
                    PrimaryAppointmentCycleStatus.DRAINING -> return true
                }
                return false
            }
            PrimaryAppointmentCycleStatus.READY -> return next == PrimaryAppointmentCycleStatus.DRAINING || next == PrimaryAppointmentCycleStatus.STOPPED
            PrimaryAppointmentCycleStatus.DRAINING -> return next == PrimaryAppointmentCycleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: PrimaryAppointmentCycleStatus) {
        if (!canMoveTo(next)) {
            error("primary appointment cycle $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "${"primary appointment cycle"} $code"

    fun weightedTotal(): Double = round(rate * updatedAt.toDouble() * 100) / 100

    fun snapshot(): PrimaryAppointmentCycle {
        return PrimaryAppointmentCycle(
            code = code,
            label = label,
            vehicleId = vehicleId,
            revision = revision,
            updatedAt = updatedAt,
            segment = segment,
            rate = rate,
            ownerId = ownerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a primary appointment cycle in status idle. */
fun newPrimaryAppointmentCycle(code: String, label: String): PrimaryAppointmentCycle {
    return PrimaryAppointmentCycle(
        code = code,
        label = label,
        vehicleId = "",
        revision = 0,
        updatedAt = 0,
        segment = "",
        rate = 0.0,
        ownerId = "",
        status = PrimaryAppointmentCycleStatus.IDLE,
    )
}
