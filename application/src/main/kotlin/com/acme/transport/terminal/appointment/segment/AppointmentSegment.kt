package com.acme.transport.terminal.appointment.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The appointment segment records of terminal's appointment work, and the rules, stores, and calculations
// around them.

/** Where a appointment segment is in its life cycle. */
enum class AppointmentSegmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A appointment segment. Its code is unique within a tenant. */
data class AppointmentSegment(
    var code: String,
    var title: String,
    var unit: String,
    var createdAt: Int,
    var maximum: Double,
    var threshold: Double,
    var durationMinutes: Int,
    var carrier: String,
    var updatedAt: Int,
    var windowDays: Int,
    var ceiling: Double,
    var status: AppointmentSegmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentSegmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentSegmentStatus): Boolean {
        when (status) {
            AppointmentSegmentStatus.PLANNED -> {
                when (next) {
                    AppointmentSegmentStatus.SCHEDULED -> return true
                }
                return false
            }
            AppointmentSegmentStatus.SCHEDULED -> {
                when (next) {
                    AppointmentSegmentStatus.COMPLETED, AppointmentSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            AppointmentSegmentStatus.IN_PROGRESS -> {
                when (next) {
                    AppointmentSegmentStatus.CANCELLED -> return true
                }
                return false
            }
            AppointmentSegmentStatus.COMPLETED -> {
                when (next) {
                    AppointmentSegmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AppointmentSegmentStatus) {
        if (!canMoveTo(next)) {
            error("appointment segment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun effectiveLoad(): Double = round((ceiling + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): AppointmentSegment {
        return AppointmentSegment(
            code = code,
            title = title,
            unit = unit,
            createdAt = createdAt,
            maximum = maximum,
            threshold = threshold,
            durationMinutes = durationMinutes,
            carrier = carrier,
            updatedAt = updatedAt,
            windowDays = windowDays,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a appointment segment in status planned. */
fun newAppointmentSegment(code: String, title: String): AppointmentSegment {
    return AppointmentSegment(
        code = code,
        title = title,
        unit = "",
        createdAt = 0,
        maximum = 0.0,
        threshold = 0.0,
        durationMinutes = 0,
        carrier = "",
        updatedAt = 0,
        windowDays = 0,
        ceiling = 0.0,
        status = AppointmentSegmentStatus.PLANNED,
    )
}
