package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback appointment group records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a fallback appointment group is in its life cycle. */
enum class FallbackAppointmentGroupStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A fallback appointment group as the appointment team keeps it. */
data class FallbackAppointmentGroup(
    var code: String,
    var label: String,
    var fuelLiters: Double,
    var attempts: Int,
    var updatedAt: Int,
    var floorValue: Double,
    var distanceKm: Double,
    var status: FallbackAppointmentGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackAppointmentGroupStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackAppointmentGroupStatus): Boolean {
        when (status) {
            FallbackAppointmentGroupStatus.PLANNED -> return next == FallbackAppointmentGroupStatus.COMPLETED || next == FallbackAppointmentGroupStatus.CANCELLED
            FallbackAppointmentGroupStatus.SCHEDULED -> return next == FallbackAppointmentGroupStatus.COMPLETED
            FallbackAppointmentGroupStatus.IN_PROGRESS -> return next == FallbackAppointmentGroupStatus.CANCELLED
            FallbackAppointmentGroupStatus.COMPLETED -> return next == FallbackAppointmentGroupStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FallbackAppointmentGroupStatus) {
        if (!canMoveTo(next)) {
            error("fallback appointment group $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedLoad(): Double = round((floorValue - attempts.toDouble()) * 100) / 100

    fun snapshot(): FallbackAppointmentGroup {
        return FallbackAppointmentGroup(
            code = code,
            label = label,
            fuelLiters = fuelLiters,
            attempts = attempts,
            updatedAt = updatedAt,
            floorValue = floorValue,
            distanceKm = distanceKm,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a fallback appointment group in status planned. */
fun newFallbackAppointmentGroup(code: String, label: String): FallbackAppointmentGroup {
    return FallbackAppointmentGroup(
        code = code,
        label = label,
        fuelLiters = 0.0,
        attempts = 0,
        updatedAt = 0,
        floorValue = 0.0,
        distanceKm = 0.0,
        status = FallbackAppointmentGroupStatus.PLANNED,
    )
}
