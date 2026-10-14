package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.text.normalize

/** Where a adaptive appointment window is in its life cycle. */
enum class AdaptiveAppointmentWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A adaptive appointment window as the appointment team keeps it. */
data class AdaptiveAppointmentWindow(
    var code: String,
    var title: String,
    var active: Boolean,
    var category: String,
    var tags: MutableList<String>,
    var score: Double,
    var status: AdaptiveAppointmentWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveAppointmentWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveAppointmentWindowStatus): Boolean {
        when (status) {
            AdaptiveAppointmentWindowStatus.OPEN -> {
                when (next) {
                    AdaptiveAppointmentWindowStatus.ARCHIVED -> return true
                }
                return false
            }
            AdaptiveAppointmentWindowStatus.IN_REVIEW -> {
                when (next) {
                    AdaptiveAppointmentWindowStatus.ARCHIVED -> return true
                }
                return false
            }
            AdaptiveAppointmentWindowStatus.RESOLVED -> return next == AdaptiveAppointmentWindowStatus.REOPENED || next == AdaptiveAppointmentWindowStatus.ARCHIVED
            AdaptiveAppointmentWindowStatus.REOPENED -> {
                when (next) {
                    AdaptiveAppointmentWindowStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveAppointmentWindowStatus) {
        if (!canMoveTo(next)) {
            error("adaptive appointment window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = score

    fun snapshot(): AdaptiveAppointmentWindow {
        return AdaptiveAppointmentWindow(
            code = code,
            title = title,
            active = active,
            category = category,
            tags = tags,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a adaptive appointment window in status open. */
fun newAdaptiveAppointmentWindow(code: String, title: String): AdaptiveAppointmentWindow {
    return AdaptiveAppointmentWindow(
        code = code,
        title = title,
        active = false,
        category = "",
        tags = mutableListOf<String>(),
        score = 0.0,
        status = AdaptiveAppointmentWindowStatus.OPEN,
    )
}
