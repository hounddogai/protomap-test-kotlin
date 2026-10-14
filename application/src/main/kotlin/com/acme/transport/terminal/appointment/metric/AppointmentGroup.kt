package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a appointment group is in its life cycle. */
enum class AppointmentGroupStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One appointment group, with what the planners need to act on it. */
data class AppointmentGroup(
    var code: String,
    var label: String,
    var maximum: Double,
    var externalRef: String,
    var source: String,
    var limit: Int,
    var routeCode: String,
    var status: AppointmentGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentGroupStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentGroupStatus): Boolean {
        when (status) {
            AppointmentGroupStatus.PROPOSED -> return next == AppointmentGroupStatus.APPROVED || next == AppointmentGroupStatus.WITHDRAWN
            AppointmentGroupStatus.APPROVED -> {
                when (next) {
                    AppointmentGroupStatus.REJECTED, AppointmentGroupStatus.WITHDRAWN -> return true
                }
                return false
            }
            AppointmentGroupStatus.REJECTED -> {
                when (next) {
                    AppointmentGroupStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentGroupStatus) {
        if (!canMoveTo(next)) {
            error("appointment group $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedLoad(): Double = round(limit.toDouble() * maximum * 100) / 100

    fun snapshot(): AppointmentGroup {
        return AppointmentGroup(
            code = code,
            label = label,
            maximum = maximum,
            externalRef = externalRef,
            source = source,
            limit = limit,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a appointment group in status proposed. */
fun newAppointmentGroup(code: String, label: String): AppointmentGroup {
    return AppointmentGroup(
        code = code,
        label = label,
        maximum = 0.0,
        externalRef = "",
        source = "",
        limit = 0,
        routeCode = "",
        status = AppointmentGroupStatus.PROPOSED,
    )
}
