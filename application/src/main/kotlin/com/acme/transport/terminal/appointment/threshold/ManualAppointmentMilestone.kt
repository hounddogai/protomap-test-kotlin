package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual appointment milestone is in its life cycle. */
enum class ManualAppointmentMilestoneStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A manual appointment milestone. Its code is unique within a tenant. */
data class ManualAppointmentMilestone(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var updatedAt: Int,
    var score: Double,
    var startsAt: Int,
    var version: Int,
    var status: ManualAppointmentMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAppointmentMilestoneStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAppointmentMilestoneStatus): Boolean {
        when (status) {
            ManualAppointmentMilestoneStatus.PLANNED -> return next == ManualAppointmentMilestoneStatus.SCHEDULED || next == ManualAppointmentMilestoneStatus.IN_PROGRESS
            ManualAppointmentMilestoneStatus.SCHEDULED -> return next == ManualAppointmentMilestoneStatus.COMPLETED || next == ManualAppointmentMilestoneStatus.CANCELLED
            ManualAppointmentMilestoneStatus.IN_PROGRESS -> return next == ManualAppointmentMilestoneStatus.CANCELLED
            ManualAppointmentMilestoneStatus.COMPLETED -> {
                when (next) {
                    ManualAppointmentMilestoneStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualAppointmentMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("manual appointment milestone $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
        version += 1
    }

    fun label(): String = "$code: $title"

    fun projectedValue(): Double = round((updatedAt.toDouble() + score) * 100) / 100

    fun snapshot(): ManualAppointmentMilestone {
        return ManualAppointmentMilestone(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            updatedAt = updatedAt,
            score = score,
            startsAt = startsAt,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a manual appointment milestone in status planned. */
fun newManualAppointmentMilestone(code: String, title: String): ManualAppointmentMilestone {
    return ManualAppointmentMilestone(
        code = code,
        title = title,
        fuelLiters = 0.0,
        updatedAt = 0,
        score = 0.0,
        startsAt = 0,
        version = 0,
        status = ManualAppointmentMilestoneStatus.PLANNED,
    )
}
