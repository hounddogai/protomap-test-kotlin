package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment plan is in its life cycle. */
enum class AppointmentPlanStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A appointment plan. Its code is unique within a tenant. */
data class AppointmentPlan(
    var code: String,
    var name: String,
    var trailerId: String,
    var graceDays: Int,
    var startsAt: Int,
    var discount: Double,
    var updatedAt: Int,
    var source: String,
    var ceiling: Double,
    var status: AppointmentPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentPlanStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentPlanStatus): Boolean {
        when (status) {
            AppointmentPlanStatus.PROPOSED -> return next == AppointmentPlanStatus.APPROVED || next == AppointmentPlanStatus.WITHDRAWN
            AppointmentPlanStatus.APPROVED -> return next == AppointmentPlanStatus.REJECTED || next == AppointmentPlanStatus.WITHDRAWN
            AppointmentPlanStatus.REJECTED -> return next == AppointmentPlanStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AppointmentPlanStatus) {
        if (!canMoveTo(next)) {
            error("appointment plan $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun effectiveVolume(): Double = round((updatedAt.toDouble() - graceDays.toDouble()) * 100) / 100

    fun snapshot(): AppointmentPlan {
        return AppointmentPlan(
            code = code,
            name = name,
            trailerId = trailerId,
            graceDays = graceDays,
            startsAt = startsAt,
            discount = discount,
            updatedAt = updatedAt,
            source = source,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a appointment plan in status proposed. */
fun newAppointmentPlan(code: String, name: String): AppointmentPlan {
    return AppointmentPlan(
        code = code,
        name = name,
        trailerId = "",
        graceDays = 0,
        startsAt = 0,
        discount = 0.0,
        updatedAt = 0,
        source = "",
        ceiling = 0.0,
        status = AppointmentPlanStatus.PROPOSED,
    )
}
