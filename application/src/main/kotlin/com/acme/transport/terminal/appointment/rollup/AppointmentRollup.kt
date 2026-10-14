package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The appointment rollup records of terminal's appointment work, and the rules, stores, and calculations
// around them.

/** Where a appointment rollup is in its life cycle. */
enum class AppointmentRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A appointment rollup as the appointment team keeps it. */
data class AppointmentRollup(
    var code: String,
    var name: String,
    var currency: String,
    var weight: Double,
    var score: Double,
    var floorValue: Double,
    var dock: String,
    var status: AppointmentRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentRollupStatus): Boolean {
        when (status) {
            AppointmentRollupStatus.REQUESTED -> {
                when (next) {
                    AppointmentRollupStatus.GRANTED -> return true
                }
                return false
            }
            AppointmentRollupStatus.GRANTED -> return next == AppointmentRollupStatus.REVOKED || next == AppointmentRollupStatus.EXPIRED
            AppointmentRollupStatus.REVOKED -> return next == AppointmentRollupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: AppointmentRollupStatus) {
        if (!canMoveTo(next)) {
            error("appointment rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun adjustedLoad(): Double = round((floorValue + score) * 100) / 100

    fun snapshot(): AppointmentRollup {
        return AppointmentRollup(
            code = code,
            name = name,
            currency = currency,
            weight = weight,
            score = score,
            floorValue = floorValue,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a appointment rollup in status requested. */
fun newAppointmentRollup(code: String, name: String): AppointmentRollup {
    return AppointmentRollup(
        code = code,
        name = name,
        currency = "",
        weight = 0.0,
        score = 0.0,
        floorValue = 0.0,
        dock = "",
        status = AppointmentRollupStatus.REQUESTED,
    )
}
