package com.acme.transport.terminal.appointment.entry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled appointment summary is in its life cycle. */
enum class ScheduledAppointmentSummaryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One scheduled appointment summary, with what the planners need to act on it. */
data class ScheduledAppointmentSummary(
    var code: String,
    var label: String,
    var country: String,
    var category: String,
    var batchSize: Int,
    var createdAt: Int,
    var description: String,
    var routeCode: String,
    var active: Boolean,
    var minimum: Double,
    var windowDays: Int,
    var status: ScheduledAppointmentSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledAppointmentSummaryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledAppointmentSummaryStatus): Boolean {
        when (status) {
            ScheduledAppointmentSummaryStatus.NEW -> return next == ScheduledAppointmentSummaryStatus.RUNNING
            ScheduledAppointmentSummaryStatus.QUEUED -> return next == ScheduledAppointmentSummaryStatus.RUNNING || next == ScheduledAppointmentSummaryStatus.DONE
            ScheduledAppointmentSummaryStatus.RUNNING -> {
                when (next) {
                    ScheduledAppointmentSummaryStatus.FAILED -> return true
                }
                return false
            }
            ScheduledAppointmentSummaryStatus.DONE -> {
                when (next) {
                    ScheduledAppointmentSummaryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledAppointmentSummaryStatus) {
        if (!canMoveTo(next)) {
            error("scheduled appointment summary $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedLoad(): Double = round((createdAt.toDouble() + minimum) * 100) / 100

    fun snapshot(): ScheduledAppointmentSummary {
        return ScheduledAppointmentSummary(
            code = code,
            label = label,
            country = country,
            category = category,
            batchSize = batchSize,
            createdAt = createdAt,
            description = description,
            routeCode = routeCode,
            active = active,
            minimum = minimum,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a scheduled appointment summary in status new. */
fun newScheduledAppointmentSummary(code: String, label: String): ScheduledAppointmentSummary {
    return ScheduledAppointmentSummary(
        code = code,
        label = label,
        country = "",
        category = "",
        batchSize = 0,
        createdAt = 0,
        description = "",
        routeCode = "",
        active = false,
        minimum = 0.0,
        windowDays = 0,
        status = ScheduledAppointmentSummaryStatus.NEW,
    )
}
