package com.acme.transport.terminal.appointment.estimate

import com.acme.transport.kit.text.normalize

// The appointment estimate records of terminal's appointment work, and the rules, stores, and calculations
// around them.

/** Where a appointment estimate is in its life cycle. */
enum class AppointmentEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A appointment estimate. Its code is unique within a tenant. */
data class AppointmentEstimate(
    var code: String,
    var label: String,
    var windowDays: Int,
    var category: String,
    var checksum: String,
    var trailerId: String,
    var reviewer: String,
    var archived: Boolean,
    var status: AppointmentEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentEstimateStatus): Boolean {
        when (status) {
            AppointmentEstimateStatus.IDLE -> {
                when (next) {
                    AppointmentEstimateStatus.READY, AppointmentEstimateStatus.DRAINING -> return true
                }
                return false
            }
            AppointmentEstimateStatus.WARMING -> return next == AppointmentEstimateStatus.READY || next == AppointmentEstimateStatus.DRAINING
            AppointmentEstimateStatus.READY -> return next == AppointmentEstimateStatus.DRAINING
            AppointmentEstimateStatus.DRAINING -> return next == AppointmentEstimateStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: AppointmentEstimateStatus) {
        if (!canMoveTo(next)) {
            error("appointment estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): AppointmentEstimate {
        return AppointmentEstimate(
            code = code,
            label = label,
            windowDays = windowDays,
            category = category,
            checksum = checksum,
            trailerId = trailerId,
            reviewer = reviewer,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a appointment estimate in status idle. */
fun newAppointmentEstimate(code: String, label: String): AppointmentEstimate {
    return AppointmentEstimate(
        code = code,
        label = label,
        windowDays = 0,
        category = "",
        checksum = "",
        trailerId = "",
        reviewer = "",
        archived = false,
        status = AppointmentEstimateStatus.IDLE,
    )
}
