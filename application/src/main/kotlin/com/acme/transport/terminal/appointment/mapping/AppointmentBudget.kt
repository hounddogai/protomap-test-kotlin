package com.acme.transport.terminal.appointment.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment budget is in its life cycle. */
enum class AppointmentBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a appointment budget and its current status. */
data class AppointmentBudget(
    var code: String,
    var title: String,
    var startsAt: Int,
    var active: Boolean,
    var capacity: Int,
    var description: String,
    var driverId: String,
    var locale: String,
    var ceiling: Double,
    var pallets: Int,
    var status: AppointmentBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentBudgetStatus): Boolean {
        when (status) {
            AppointmentBudgetStatus.OPEN -> return next == AppointmentBudgetStatus.IN_REVIEW || next == AppointmentBudgetStatus.REOPENED
            AppointmentBudgetStatus.IN_REVIEW -> {
                when (next) {
                    AppointmentBudgetStatus.REOPENED, AppointmentBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
            AppointmentBudgetStatus.RESOLVED -> {
                when (next) {
                    AppointmentBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
            AppointmentBudgetStatus.REOPENED -> return next == AppointmentBudgetStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: AppointmentBudgetStatus) {
        if (!canMoveTo(next)) {
            error("appointment budget $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun netTotal(): Double = round(startsAt.toDouble() * capacity.toDouble() * 100) / 100

    fun snapshot(): AppointmentBudget {
        return AppointmentBudget(
            code = code,
            title = title,
            startsAt = startsAt,
            active = active,
            capacity = capacity,
            description = description,
            driverId = driverId,
            locale = locale,
            ceiling = ceiling,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a appointment budget in status open. */
fun newAppointmentBudget(code: String, title: String): AppointmentBudget {
    return AppointmentBudget(
        code = code,
        title = title,
        startsAt = 0,
        active = false,
        capacity = 0,
        description = "",
        driverId = "",
        locale = "",
        ceiling = 0.0,
        pallets = 0,
        status = AppointmentBudgetStatus.OPEN,
    )
}
