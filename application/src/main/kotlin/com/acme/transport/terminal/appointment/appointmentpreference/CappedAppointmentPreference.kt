package com.acme.transport.terminal.appointment.appointmentpreference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped appointment preference records of terminal's appointment work, and the rules, stores, and
// calculations around them.

/** Where a capped appointment preference is in its life cycle. */
enum class CappedAppointmentPreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a capped appointment preference and its current status. */
data class CappedAppointmentPreference(
    var code: String,
    var label: String,
    var attempts: Int,
    var active: Boolean,
    var title: String,
    var weight: Double,
    var summary: String,
    var status: CappedAppointmentPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedAppointmentPreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedAppointmentPreferenceStatus): Boolean {
        when (status) {
            CappedAppointmentPreferenceStatus.PROPOSED -> return next == CappedAppointmentPreferenceStatus.REJECTED
            CappedAppointmentPreferenceStatus.APPROVED -> {
                when (next) {
                    CappedAppointmentPreferenceStatus.REJECTED -> return true
                }
                return false
            }
            CappedAppointmentPreferenceStatus.REJECTED -> {
                when (next) {
                    CappedAppointmentPreferenceStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedAppointmentPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("capped appointment preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun adjustedLoad(): Double = round(attempts.toDouble() * weight * 100) / 100

    fun snapshot(): CappedAppointmentPreference {
        return CappedAppointmentPreference(
            code = code,
            label = label,
            attempts = attempts,
            active = active,
            title = title,
            weight = weight,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a capped appointment preference in status proposed. */
fun newCappedAppointmentPreference(code: String, label: String): CappedAppointmentPreference {
    return CappedAppointmentPreference(
        code = code,
        label = label,
        attempts = 0,
        active = false,
        title = "",
        weight = 0.0,
        summary = "",
        status = CappedAppointmentPreferenceStatus.PROPOSED,
    )
}
