package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted appointment plan is in its life cycle. */
enum class WeightedAppointmentPlanStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One weighted appointment plan, with what the planners need to act on it. */
data class WeightedAppointmentPlan(
    var code: String,
    var label: String,
    var active: Boolean,
    var distanceKm: Double,
    var pallets: Int,
    var summary: String,
    var pinned: Boolean,
    var ratio: Double,
    var lane: String,
    var status: WeightedAppointmentPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAppointmentPlanStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAppointmentPlanStatus): Boolean {
        when (status) {
            WeightedAppointmentPlanStatus.IDLE -> return next == WeightedAppointmentPlanStatus.READY || next == WeightedAppointmentPlanStatus.STOPPED
            WeightedAppointmentPlanStatus.WARMING -> {
                when (next) {
                    WeightedAppointmentPlanStatus.STOPPED -> return true
                }
                return false
            }
            WeightedAppointmentPlanStatus.READY -> {
                when (next) {
                    WeightedAppointmentPlanStatus.DRAINING, WeightedAppointmentPlanStatus.STOPPED -> return true
                }
                return false
            }
            WeightedAppointmentPlanStatus.DRAINING -> return next == WeightedAppointmentPlanStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: WeightedAppointmentPlanStatus) {
        if (!canMoveTo(next)) {
            error("weighted appointment plan $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveTotal(): Double = round(ratio * distanceKm * 100) / 100

    fun snapshot(): WeightedAppointmentPlan {
        return WeightedAppointmentPlan(
            code = code,
            label = label,
            active = active,
            distanceKm = distanceKm,
            pallets = pallets,
            summary = summary,
            pinned = pinned,
            ratio = ratio,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a weighted appointment plan in status idle. */
fun newWeightedAppointmentPlan(code: String, label: String): WeightedAppointmentPlan {
    return WeightedAppointmentPlan(
        code = code,
        label = label,
        active = false,
        distanceKm = 0.0,
        pallets = 0,
        summary = "",
        pinned = false,
        ratio = 0.0,
        lane = "",
        status = WeightedAppointmentPlanStatus.IDLE,
    )
}
