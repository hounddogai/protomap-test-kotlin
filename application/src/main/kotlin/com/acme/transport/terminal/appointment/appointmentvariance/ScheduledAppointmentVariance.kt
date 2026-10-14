package com.acme.transport.terminal.appointment.appointmentvariance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled appointment variance records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a scheduled appointment variance is in its life cycle. */
enum class ScheduledAppointmentVarianceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled appointment variance. Its code is unique within a tenant. */
data class ScheduledAppointmentVariance(
    var code: String,
    var title: String,
    var rate: Double,
    var externalRef: String,
    var capacity: Int,
    var checksum: String,
    var updatedAt: Int,
    var status: ScheduledAppointmentVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledAppointmentVarianceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledAppointmentVarianceStatus): Boolean {
        when (status) {
            ScheduledAppointmentVarianceStatus.OPEN -> return next == ScheduledAppointmentVarianceStatus.RESOLVED || next == ScheduledAppointmentVarianceStatus.REOPENED
            ScheduledAppointmentVarianceStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledAppointmentVarianceStatus.REOPENED, ScheduledAppointmentVarianceStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledAppointmentVarianceStatus.RESOLVED -> return next == ScheduledAppointmentVarianceStatus.REOPENED || next == ScheduledAppointmentVarianceStatus.ARCHIVED
            ScheduledAppointmentVarianceStatus.REOPENED -> return next == ScheduledAppointmentVarianceStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledAppointmentVarianceStatus) {
        if (!canMoveTo(next)) {
            error("scheduled appointment variance $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun weightedLoad(): Double = round(updatedAt.toDouble() * rate * 100) / 100

    fun snapshot(): ScheduledAppointmentVariance {
        return ScheduledAppointmentVariance(
            code = code,
            title = title,
            rate = rate,
            externalRef = externalRef,
            capacity = capacity,
            checksum = checksum,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scheduled appointment variance in status open. */
fun newScheduledAppointmentVariance(code: String, title: String): ScheduledAppointmentVariance {
    return ScheduledAppointmentVariance(
        code = code,
        title = title,
        rate = 0.0,
        externalRef = "",
        capacity = 0,
        checksum = "",
        updatedAt = 0,
        status = ScheduledAppointmentVarianceStatus.OPEN,
    )
}
