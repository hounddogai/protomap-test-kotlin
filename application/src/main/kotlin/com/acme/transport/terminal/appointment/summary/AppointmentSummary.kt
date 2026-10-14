package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The appointment summary records of terminal's appointment work, and the rules, stores, and calculations
// around them.

/** Where a appointment summary is in its life cycle. */
enum class AppointmentSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One appointment summary, with what the planners need to act on it. */
data class AppointmentSummary(
    var code: String,
    var name: String,
    var channel: String,
    var priority: Int,
    var phone: String,
    var attempts: Int,
    var description: String,
    var tags: MutableList<String>,
    var revision: Int,
    var status: AppointmentSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentSummaryStatus): Boolean {
        when (status) {
            AppointmentSummaryStatus.PROPOSED -> return next == AppointmentSummaryStatus.APPROVED || next == AppointmentSummaryStatus.REJECTED
            AppointmentSummaryStatus.APPROVED -> {
                when (next) {
                    AppointmentSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
            AppointmentSummaryStatus.REJECTED -> return next == AppointmentSummaryStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AppointmentSummaryStatus) {
        if (!canMoveTo(next)) {
            error("appointment summary $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$name]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedVolume(): Double = round((attempts.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): AppointmentSummary {
        return AppointmentSummary(
            code = code,
            name = name,
            channel = channel,
            priority = priority,
            phone = phone,
            attempts = attempts,
            description = description,
            tags = tags,
            revision = revision,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a appointment summary in status proposed. */
fun newAppointmentSummary(code: String, name: String): AppointmentSummary {
    return AppointmentSummary(
        code = code,
        name = name,
        channel = "",
        priority = 0,
        phone = "",
        attempts = 0,
        description = "",
        tags = mutableListOf<String>(),
        revision = 0,
        status = AppointmentSummaryStatus.PROPOSED,
    )
}
