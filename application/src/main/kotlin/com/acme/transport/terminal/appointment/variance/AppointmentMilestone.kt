package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment milestone is in its life cycle. */
enum class AppointmentMilestoneStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A appointment milestone as the appointment team keeps it. */
data class AppointmentMilestone(
    var code: String,
    var label: String,
    var contactEmail: String,
    var odometer: Int,
    var quantity: Int,
    var driverId: String,
    var status: AppointmentMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentMilestoneStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentMilestoneStatus): Boolean {
        when (status) {
            AppointmentMilestoneStatus.IDLE -> {
                when (next) {
                    AppointmentMilestoneStatus.STOPPED -> return true
                }
                return false
            }
            AppointmentMilestoneStatus.WARMING -> return next == AppointmentMilestoneStatus.READY || next == AppointmentMilestoneStatus.STOPPED
            AppointmentMilestoneStatus.READY -> return next == AppointmentMilestoneStatus.STOPPED
            AppointmentMilestoneStatus.DRAINING -> {
                when (next) {
                    AppointmentMilestoneStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("appointment milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedVolume(): Double = round((quantity.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): AppointmentMilestone {
        return AppointmentMilestone(
            code = code,
            label = label,
            contactEmail = contactEmail,
            odometer = odometer,
            quantity = quantity,
            driverId = driverId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a appointment milestone in status idle. */
fun newAppointmentMilestone(code: String, label: String): AppointmentMilestone {
    return AppointmentMilestone(
        code = code,
        label = label,
        contactEmail = "",
        odometer = 0,
        quantity = 0,
        driverId = "",
        status = AppointmentMilestoneStatus.IDLE,
    )
}
