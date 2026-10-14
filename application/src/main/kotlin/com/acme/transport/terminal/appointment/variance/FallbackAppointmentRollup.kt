package com.acme.transport.terminal.appointment.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback appointment rollup is in its life cycle. */
enum class FallbackAppointmentRollupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fallback appointment rollup as the appointment team keeps it. */
data class FallbackAppointmentRollup(
    var code: String,
    var name: String,
    var discount: Double,
    var windowDays: Int,
    var trailerId: String,
    var createdAt: Int,
    var expiresAt: Int,
    var segment: String,
    var unit: String,
    var timezone: String,
    var locale: String,
    var status: FallbackAppointmentRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackAppointmentRollupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackAppointmentRollupStatus): Boolean {
        when (status) {
            FallbackAppointmentRollupStatus.OPEN -> return next == FallbackAppointmentRollupStatus.RESOLVED || next == FallbackAppointmentRollupStatus.REOPENED
            FallbackAppointmentRollupStatus.IN_REVIEW -> return next == FallbackAppointmentRollupStatus.RESOLVED || next == FallbackAppointmentRollupStatus.REOPENED
            FallbackAppointmentRollupStatus.RESOLVED -> return next == FallbackAppointmentRollupStatus.ARCHIVED
            FallbackAppointmentRollupStatus.REOPENED -> return next == FallbackAppointmentRollupStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FallbackAppointmentRollupStatus) {
        if (!canMoveTo(next)) {
            error("fallback appointment rollup $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun effectiveLoad(): Double = round((expiresAt.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): FallbackAppointmentRollup {
        return FallbackAppointmentRollup(
            code = code,
            name = name,
            discount = discount,
            windowDays = windowDays,
            trailerId = trailerId,
            createdAt = createdAt,
            expiresAt = expiresAt,
            segment = segment,
            unit = unit,
            timezone = timezone,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a fallback appointment rollup in status open. */
fun newFallbackAppointmentRollup(code: String, name: String): FallbackAppointmentRollup {
    return FallbackAppointmentRollup(
        code = code,
        name = name,
        discount = 0.0,
        windowDays = 0,
        trailerId = "",
        createdAt = 0,
        expiresAt = 0,
        segment = "",
        unit = "",
        timezone = "",
        locale = "",
        status = FallbackAppointmentRollupStatus.OPEN,
    )
}
