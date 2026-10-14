package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict appointment summary is in its life cycle. */
enum class StrictAppointmentSummaryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A strict appointment summary as the appointment team keeps it. */
data class StrictAppointmentSummary(
    var code: String,
    var title: String,
    var windowDays: Int,
    var ceiling: Double,
    var cost: Double,
    var graceDays: Int,
    var expiresAt: Int,
    var minimum: Double,
    var status: StrictAppointmentSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictAppointmentSummaryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictAppointmentSummaryStatus): Boolean {
        when (status) {
            StrictAppointmentSummaryStatus.DRAFT -> {
                when (next) {
                    StrictAppointmentSummaryStatus.PENDING, StrictAppointmentSummaryStatus.CLOSED -> return true
                }
                return false
            }
            StrictAppointmentSummaryStatus.PENDING -> {
                when (next) {
                    StrictAppointmentSummaryStatus.ACTIVE, StrictAppointmentSummaryStatus.SUSPENDED -> return true
                }
                return false
            }
            StrictAppointmentSummaryStatus.ACTIVE -> {
                when (next) {
                    StrictAppointmentSummaryStatus.SUSPENDED, StrictAppointmentSummaryStatus.CLOSED -> return true
                }
                return false
            }
            StrictAppointmentSummaryStatus.SUSPENDED -> return next == StrictAppointmentSummaryStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: StrictAppointmentSummaryStatus) {
        if (!canMoveTo(next)) {
            error("strict appointment summary $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun effectiveTotal(): Double = round(minimum * expiresAt.toDouble() * 100) / 100

    fun snapshot(): StrictAppointmentSummary {
        return StrictAppointmentSummary(
            code = code,
            title = title,
            windowDays = windowDays,
            ceiling = ceiling,
            cost = cost,
            graceDays = graceDays,
            expiresAt = expiresAt,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a strict appointment summary in status draft. */
fun newStrictAppointmentSummary(code: String, title: String): StrictAppointmentSummary {
    return StrictAppointmentSummary(
        code = code,
        title = title,
        windowDays = 0,
        ceiling = 0.0,
        cost = 0.0,
        graceDays = 0,
        expiresAt = 0,
        minimum = 0.0,
        status = StrictAppointmentSummaryStatus.DRAFT,
    )
}
