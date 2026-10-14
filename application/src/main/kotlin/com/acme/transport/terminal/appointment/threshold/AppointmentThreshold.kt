package com.acme.transport.terminal.appointment.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The appointment threshold records of terminal's appointment work, and the rules, stores, and calculations
// around them.

/** Where a appointment threshold is in its life cycle. */
enum class AppointmentThresholdStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a appointment threshold and its current status. */
data class AppointmentThreshold(
    var code: String,
    var title: String,
    var quantity: Int,
    var etaMinutes: Int,
    var threshold: Double,
    var margin: Double,
    var minimum: Double,
    var limit: Int,
    var phone: String,
    var country: String,
    var status: AppointmentThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentThresholdStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentThresholdStatus): Boolean {
        when (status) {
            AppointmentThresholdStatus.OPEN -> return next == AppointmentThresholdStatus.IN_REVIEW
            AppointmentThresholdStatus.IN_REVIEW -> return next == AppointmentThresholdStatus.REOPENED || next == AppointmentThresholdStatus.ARCHIVED
            AppointmentThresholdStatus.RESOLVED -> return next == AppointmentThresholdStatus.REOPENED || next == AppointmentThresholdStatus.ARCHIVED
            AppointmentThresholdStatus.REOPENED -> {
                when (next) {
                    AppointmentThresholdStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentThresholdStatus) {
        if (!canMoveTo(next)) {
            error("appointment threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"appointment threshold"} $code"

    fun netTotal(): Double = round(minimum * threshold * 100) / 100

    fun snapshot(): AppointmentThreshold {
        return AppointmentThreshold(
            code = code,
            title = title,
            quantity = quantity,
            etaMinutes = etaMinutes,
            threshold = threshold,
            margin = margin,
            minimum = minimum,
            limit = limit,
            phone = phone,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a appointment threshold in status open. */
fun newAppointmentThreshold(code: String, title: String): AppointmentThreshold {
    return AppointmentThreshold(
        code = code,
        title = title,
        quantity = 0,
        etaMinutes = 0,
        threshold = 0.0,
        margin = 0.0,
        minimum = 0.0,
        limit = 0,
        phone = "",
        country = "",
        status = AppointmentThresholdStatus.OPEN,
    )
}
