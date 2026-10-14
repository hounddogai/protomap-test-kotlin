package com.acme.transport.terminal.appointment.appointmentrollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global appointment rollup records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a global appointment rollup is in its life cycle. */
enum class GlobalAppointmentRollupStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a global appointment rollup and its current status. */
data class GlobalAppointmentRollup(
    var code: String,
    var title: String,
    var reviewer: String,
    var windowDays: Int,
    var updatedAt: Int,
    var routeCode: String,
    var minimum: Double,
    var approved: Boolean,
    var capacity: Int,
    var driverId: String,
    var quantity: Int,
    var status: GlobalAppointmentRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalAppointmentRollupStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalAppointmentRollupStatus): Boolean {
        when (status) {
            GlobalAppointmentRollupStatus.CREATED -> return next == GlobalAppointmentRollupStatus.PUBLISHED || next == GlobalAppointmentRollupStatus.RETIRED
            GlobalAppointmentRollupStatus.VALIDATED -> return next == GlobalAppointmentRollupStatus.PUBLISHED || next == GlobalAppointmentRollupStatus.RETIRED
            GlobalAppointmentRollupStatus.PUBLISHED -> {
                when (next) {
                    GlobalAppointmentRollupStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalAppointmentRollupStatus) {
        if (!canMoveTo(next)) {
            error("global appointment rollup $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedValue(): Double = round(updatedAt.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): GlobalAppointmentRollup {
        return GlobalAppointmentRollup(
            code = code,
            title = title,
            reviewer = reviewer,
            windowDays = windowDays,
            updatedAt = updatedAt,
            routeCode = routeCode,
            minimum = minimum,
            approved = approved,
            capacity = capacity,
            driverId = driverId,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global appointment rollup in status created. */
fun newGlobalAppointmentRollup(code: String, title: String): GlobalAppointmentRollup {
    return GlobalAppointmentRollup(
        code = code,
        title = title,
        reviewer = "",
        windowDays = 0,
        updatedAt = 0,
        routeCode = "",
        minimum = 0.0,
        approved = false,
        capacity = 0,
        driverId = "",
        quantity = 0,
        status = GlobalAppointmentRollupStatus.CREATED,
    )
}
