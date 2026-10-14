package com.acme.transport.terminal.appointment.seasonalappointmentvariance

import com.acme.transport.kit.text.normalize

// The pending appointment variance records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a pending appointment variance is in its life cycle. */
enum class PendingAppointmentVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One pending appointment variance, with what the planners need to act on it. */
data class PendingAppointmentVariance(
    var code: String,
    var name: String,
    var category: String,
    var title: String,
    var phone: String,
    var quantity: Int,
    var status: PendingAppointmentVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingAppointmentVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingAppointmentVarianceStatus): Boolean {
        when (status) {
            PendingAppointmentVarianceStatus.PLANNED -> return next == PendingAppointmentVarianceStatus.IN_PROGRESS || next == PendingAppointmentVarianceStatus.CANCELLED
            PendingAppointmentVarianceStatus.SCHEDULED -> {
                when (next) {
                    PendingAppointmentVarianceStatus.IN_PROGRESS, PendingAppointmentVarianceStatus.CANCELLED -> return true
                }
                return false
            }
            PendingAppointmentVarianceStatus.IN_PROGRESS -> return next == PendingAppointmentVarianceStatus.COMPLETED || next == PendingAppointmentVarianceStatus.CANCELLED
            PendingAppointmentVarianceStatus.COMPLETED -> {
                when (next) {
                    PendingAppointmentVarianceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingAppointmentVarianceStatus) {
        if (!canMoveTo(next)) {
            error("pending appointment variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): PendingAppointmentVariance {
        return PendingAppointmentVariance(
            code = code,
            name = name,
            category = category,
            title = title,
            phone = phone,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a pending appointment variance in status planned. */
fun newPendingAppointmentVariance(code: String, name: String): PendingAppointmentVariance {
    return PendingAppointmentVariance(
        code = code,
        name = name,
        category = "",
        title = "",
        phone = "",
        quantity = 0,
        status = PendingAppointmentVarianceStatus.PLANNED,
    )
}
