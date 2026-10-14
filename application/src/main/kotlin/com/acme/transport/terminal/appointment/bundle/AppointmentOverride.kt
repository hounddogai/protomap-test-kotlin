package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment override is in its life cycle. */
enum class AppointmentOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A appointment override as the appointment team keeps it. */
data class AppointmentOverride(
    var code: String,
    var name: String,
    var fuelLiters: Double,
    var score: Double,
    var batchSize: Int,
    var status: AppointmentOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentOverrideStatus): Boolean {
        when (status) {
            AppointmentOverrideStatus.REQUESTED -> {
                when (next) {
                    AppointmentOverrideStatus.GRANTED, AppointmentOverrideStatus.EXPIRED -> return true
                }
                return false
            }
            AppointmentOverrideStatus.GRANTED -> {
                when (next) {
                    AppointmentOverrideStatus.EXPIRED -> return true
                }
                return false
            }
            AppointmentOverrideStatus.REVOKED -> return next == AppointmentOverrideStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AppointmentOverrideStatus) {
        if (!canMoveTo(next)) {
            error("appointment override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun weightedValue(): Double = round(score * fuelLiters * 100) / 100

    fun snapshot(): AppointmentOverride {
        return AppointmentOverride(
            code = code,
            name = name,
            fuelLiters = fuelLiters,
            score = score,
            batchSize = batchSize,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a appointment override in status requested. */
fun newAppointmentOverride(code: String, name: String): AppointmentOverride {
    return AppointmentOverride(
        code = code,
        name = name,
        fuelLiters = 0.0,
        score = 0.0,
        batchSize = 0,
        status = AppointmentOverrideStatus.REQUESTED,
    )
}
