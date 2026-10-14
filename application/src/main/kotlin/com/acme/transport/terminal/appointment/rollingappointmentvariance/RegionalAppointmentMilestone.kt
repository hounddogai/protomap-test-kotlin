package com.acme.transport.terminal.appointment.rollingappointmentvariance

import com.acme.transport.kit.text.normalize

/** Where a regional appointment milestone is in its life cycle. */
enum class RegionalAppointmentMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A regional appointment milestone. Its code is unique within a tenant. */
data class RegionalAppointmentMilestone(
    var code: String,
    var title: String,
    var vehicleId: String,
    var revision: Int,
    var trailerId: String,
    var summary: String,
    var status: RegionalAppointmentMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalAppointmentMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalAppointmentMilestoneStatus): Boolean {
        when (status) {
            RegionalAppointmentMilestoneStatus.CREATED -> return next == RegionalAppointmentMilestoneStatus.VALIDATED
            RegionalAppointmentMilestoneStatus.VALIDATED -> return next == RegionalAppointmentMilestoneStatus.PUBLISHED || next == RegionalAppointmentMilestoneStatus.RETIRED
            RegionalAppointmentMilestoneStatus.PUBLISHED -> return next == RegionalAppointmentMilestoneStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: RegionalAppointmentMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("regional appointment milestone $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = revision.toDouble()

    fun snapshot(): RegionalAppointmentMilestone {
        return RegionalAppointmentMilestone(
            code = code,
            title = title,
            vehicleId = vehicleId,
            revision = revision,
            trailerId = trailerId,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a regional appointment milestone in status created. */
fun newRegionalAppointmentMilestone(code: String, title: String): RegionalAppointmentMilestone {
    return RegionalAppointmentMilestone(
        code = code,
        title = title,
        vehicleId = "",
        revision = 0,
        trailerId = "",
        summary = "",
        status = RegionalAppointmentMilestoneStatus.CREATED,
    )
}
