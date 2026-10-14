package com.acme.transport.terminal.appointment.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The appointment preference records of terminal's appointment work, and the rules, stores, and calculations
// around them.

/** Where a appointment preference is in its life cycle. */
enum class AppointmentPreferenceStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One appointment preference, with what the planners need to act on it. */
data class AppointmentPreference(
    var code: String,
    var label: String,
    var phone: String,
    var tags: MutableList<String>,
    var channel: String,
    var weight: Double,
    var budget: Double,
    var status: AppointmentPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AppointmentPreferenceStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: AppointmentPreferenceStatus): Boolean {
        when (status) {
            AppointmentPreferenceStatus.CREATED -> {
                when (next) {
                    AppointmentPreferenceStatus.RETIRED -> return true
                }
                return false
            }
            AppointmentPreferenceStatus.VALIDATED -> {
                when (next) {
                    AppointmentPreferenceStatus.PUBLISHED -> return true
                }
                return false
            }
            AppointmentPreferenceStatus.PUBLISHED -> return next == AppointmentPreferenceStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: AppointmentPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("appointment preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netLoad(): Double = round(budget * weight * 100) / 100

    fun snapshot(): AppointmentPreference {
        return AppointmentPreference(
            code = code,
            label = label,
            phone = phone,
            tags = tags,
            channel = channel,
            weight = weight,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a appointment preference in status created. */
fun newAppointmentPreference(code: String, label: String): AppointmentPreference {
    return AppointmentPreference(
        code = code,
        label = label,
        phone = "",
        tags = mutableListOf<String>(),
        channel = "",
        weight = 0.0,
        budget = 0.0,
        status = AppointmentPreferenceStatus.CREATED,
    )
}
