package com.acme.transport.terminal.appointment.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered appointment budget is in its life cycle. */
enum class TieredAppointmentBudgetStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One tiered appointment budget, with what the planners need to act on it. */
data class TieredAppointmentBudget(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var ratio: Double,
    var pinned: Boolean,
    var summary: String,
    var status: TieredAppointmentBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredAppointmentBudgetStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredAppointmentBudgetStatus): Boolean {
        when (status) {
            TieredAppointmentBudgetStatus.OPEN -> return next == TieredAppointmentBudgetStatus.REOPENED
            TieredAppointmentBudgetStatus.IN_REVIEW -> return next == TieredAppointmentBudgetStatus.ARCHIVED
            TieredAppointmentBudgetStatus.RESOLVED -> {
                when (next) {
                    TieredAppointmentBudgetStatus.REOPENED, TieredAppointmentBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
            TieredAppointmentBudgetStatus.REOPENED -> {
                when (next) {
                    TieredAppointmentBudgetStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredAppointmentBudgetStatus) {
        if (!canMoveTo(next)) {
            error("tiered appointment budget $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered appointment budget"} $code"

    fun projectedExposure(): Double = round(etaMinutes.toDouble() * ratio * 100) / 100

    fun snapshot(): TieredAppointmentBudget {
        return TieredAppointmentBudget(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            ratio = ratio,
            pinned = pinned,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered appointment budget in status open. */
fun newTieredAppointmentBudget(code: String, name: String): TieredAppointmentBudget {
    return TieredAppointmentBudget(
        code = code,
        name = name,
        etaMinutes = 0,
        ratio = 0.0,
        pinned = false,
        summary = "",
        status = TieredAppointmentBudgetStatus.OPEN,
    )
}
