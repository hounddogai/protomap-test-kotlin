package com.acme.transport.terminal.appointment.baseline

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive appointment budget is in its life cycle. */
enum class AdaptiveAppointmentBudgetStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One adaptive appointment budget, with what the planners need to act on it. */
data class AdaptiveAppointmentBudget(
    var code: String,
    var title: String,
    var threshold: Double,
    var name: String,
    var distanceKm: Double,
    var routeCode: String,
    var endsAt: Int,
    var quantity: Int,
    var status: AdaptiveAppointmentBudgetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAppointmentBudgetStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAppointmentBudgetStatus): Boolean {
        when (status) {
            AdaptiveAppointmentBudgetStatus.NEW -> {
                when (next) {
                    AdaptiveAppointmentBudgetStatus.QUEUED, AdaptiveAppointmentBudgetStatus.FAILED -> return true
                }
                return false
            }
            AdaptiveAppointmentBudgetStatus.QUEUED -> return next == AdaptiveAppointmentBudgetStatus.DONE || next == AdaptiveAppointmentBudgetStatus.FAILED
            AdaptiveAppointmentBudgetStatus.RUNNING -> return next == AdaptiveAppointmentBudgetStatus.DONE || next == AdaptiveAppointmentBudgetStatus.FAILED
            AdaptiveAppointmentBudgetStatus.DONE -> {
                when (next) {
                    AdaptiveAppointmentBudgetStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveAppointmentBudgetStatus) {
        if (!canMoveTo(next)) {
            error("adaptive appointment budget $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedTotal(): Double = round((distanceKm + endsAt.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveAppointmentBudget {
        return AdaptiveAppointmentBudget(
            code = code,
            title = title,
            threshold = threshold,
            name = name,
            distanceKm = distanceKm,
            routeCode = routeCode,
            endsAt = endsAt,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a adaptive appointment budget in status new. */
fun newAdaptiveAppointmentBudget(code: String, title: String): AdaptiveAppointmentBudget {
    return AdaptiveAppointmentBudget(
        code = code,
        title = title,
        threshold = 0.0,
        name = "",
        distanceKm = 0.0,
        routeCode = "",
        endsAt = 0,
        quantity = 0,
        status = AdaptiveAppointmentBudgetStatus.NEW,
    )
}
