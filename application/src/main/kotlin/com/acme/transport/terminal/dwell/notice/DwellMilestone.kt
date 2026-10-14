package com.acme.transport.terminal.dwell.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dwell milestone is in its life cycle. */
enum class DwellMilestoneStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a dwell milestone and its current status. */
data class DwellMilestone(
    var code: String,
    var label: String,
    var source: String,
    var odometer: Int,
    var durationMinutes: Int,
    var timezone: String,
    var routeCode: String,
    var contactEmail: String,
    var status: DwellMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DwellMilestoneStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: DwellMilestoneStatus): Boolean {
        when (status) {
            DwellMilestoneStatus.NEW -> {
                when (next) {
                    DwellMilestoneStatus.RUNNING, DwellMilestoneStatus.FAILED -> return true
                }
                return false
            }
            DwellMilestoneStatus.QUEUED -> return next == DwellMilestoneStatus.RUNNING
            DwellMilestoneStatus.RUNNING -> return next == DwellMilestoneStatus.DONE
            DwellMilestoneStatus.DONE -> {
                when (next) {
                    DwellMilestoneStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DwellMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("dwell milestone $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedTotal(): Double = round(durationMinutes.toDouble() * odometer.toDouble() * 100) / 100

    fun snapshot(): DwellMilestone {
        return DwellMilestone(
            code = code,
            label = label,
            source = source,
            odometer = odometer,
            durationMinutes = durationMinutes,
            timezone = timezone,
            routeCode = routeCode,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a dwell milestone in status new. */
fun newDwellMilestone(code: String, label: String): DwellMilestone {
    return DwellMilestone(
        code = code,
        label = label,
        source = "",
        odometer = 0,
        durationMinutes = 0,
        timezone = "",
        routeCode = "",
        contactEmail = "",
        status = DwellMilestoneStatus.NEW,
    )
}
