package com.acme.transport.terminal.appointment.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The appointment mapping records of terminal's appointment work, and the rules, stores, and calculations
// around them.

/** Where a appointment mapping is in its life cycle. */
enum class AppointmentMappingStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A appointment mapping as the appointment team keeps it. */
data class AppointmentMapping(
    var code: String,
    var title: String,
    var windowDays: Int,
    var batchSize: Int,
    var margin: Double,
    var label: String,
    var graceDays: Int,
    var budget: Double,
    var startsAt: Int,
    var status: AppointmentMappingStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentMappingStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentMappingStatus): Boolean {
        when (status) {
            AppointmentMappingStatus.DRAFT -> return next == AppointmentMappingStatus.ACTIVE || next == AppointmentMappingStatus.SUSPENDED
            AppointmentMappingStatus.PENDING -> return next == AppointmentMappingStatus.ACTIVE || next == AppointmentMappingStatus.SUSPENDED
            AppointmentMappingStatus.ACTIVE -> return next == AppointmentMappingStatus.SUSPENDED || next == AppointmentMappingStatus.CLOSED
            AppointmentMappingStatus.SUSPENDED -> {
                when (next) {
                    AppointmentMappingStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentMappingStatus) {
        if (!canMoveTo(next)) {
            error("appointment mapping $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"appointment mapping"} $code"

    fun netValue(): Double = round((budget - graceDays.toDouble()) * 100) / 100

    fun snapshot(): AppointmentMapping {
        return AppointmentMapping(
            code = code,
            title = title,
            windowDays = windowDays,
            batchSize = batchSize,
            margin = margin,
            label = label,
            graceDays = graceDays,
            budget = budget,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a appointment mapping in status draft. */
fun newAppointmentMapping(code: String, title: String): AppointmentMapping {
    return AppointmentMapping(
        code = code,
        title = title,
        windowDays = 0,
        batchSize = 0,
        margin = 0.0,
        label = "",
        graceDays = 0,
        budget = 0.0,
        startsAt = 0,
        status = AppointmentMappingStatus.DRAFT,
    )
}
