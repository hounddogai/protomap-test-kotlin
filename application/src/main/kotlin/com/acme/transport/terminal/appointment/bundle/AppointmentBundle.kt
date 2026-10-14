package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The appointment bundle records of terminal's appointment work, and the rules, stores, and calculations
// around them.

/** Where a appointment bundle is in its life cycle. */
enum class AppointmentBundleStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A appointment bundle as the appointment team keeps it. */
data class AppointmentBundle(
    var code: String,
    var label: String,
    var vehicleId: String,
    var carrier: String,
    var etaMinutes: Int,
    var routeCode: String,
    var budget: Double,
    var rate: Double,
    var active: Boolean,
    var reviewer: String,
    var status: AppointmentBundleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentBundleStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentBundleStatus): Boolean {
        when (status) {
            AppointmentBundleStatus.NEW -> {
                when (next) {
                    AppointmentBundleStatus.RUNNING, AppointmentBundleStatus.DONE -> return true
                }
                return false
            }
            AppointmentBundleStatus.QUEUED -> return next == AppointmentBundleStatus.FAILED
            AppointmentBundleStatus.RUNNING -> {
                when (next) {
                    AppointmentBundleStatus.DONE, AppointmentBundleStatus.FAILED -> return true
                }
                return false
            }
            AppointmentBundleStatus.DONE -> return next == AppointmentBundleStatus.FAILED
        }
        return false
    }

    fun moveTo(next: AppointmentBundleStatus) {
        if (!canMoveTo(next)) {
            error("appointment bundle $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"appointment bundle"} $code"

    fun projectedExposure(): Double = round((rate + budget) * 100) / 100

    fun snapshot(): AppointmentBundle {
        return AppointmentBundle(
            code = code,
            label = label,
            vehicleId = vehicleId,
            carrier = carrier,
            etaMinutes = etaMinutes,
            routeCode = routeCode,
            budget = budget,
            rate = rate,
            active = active,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a appointment bundle in status new. */
fun newAppointmentBundle(code: String, label: String): AppointmentBundle {
    return AppointmentBundle(
        code = code,
        label = label,
        vehicleId = "",
        carrier = "",
        etaMinutes = 0,
        routeCode = "",
        budget = 0.0,
        rate = 0.0,
        active = false,
        reviewer = "",
        status = AppointmentBundleStatus.NEW,
    )
}
