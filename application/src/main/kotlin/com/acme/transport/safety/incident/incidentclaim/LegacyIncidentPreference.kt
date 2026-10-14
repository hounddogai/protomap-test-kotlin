package com.acme.transport.safety.incident.incidentclaim

import com.acme.transport.kit.text.normalize

/** Where a legacy incident preference is in its life cycle. */
enum class LegacyIncidentPreferenceStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A legacy incident preference as the incident team keeps it. */
data class LegacyIncidentPreference(
    var code: String,
    var label: String,
    var description: String,
    var odometer: Int,
    var timezone: String,
    var status: LegacyIncidentPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyIncidentPreferenceStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyIncidentPreferenceStatus): Boolean {
        when (status) {
            LegacyIncidentPreferenceStatus.DRAFT -> return next == LegacyIncidentPreferenceStatus.PENDING || next == LegacyIncidentPreferenceStatus.ACTIVE
            LegacyIncidentPreferenceStatus.PENDING -> return next == LegacyIncidentPreferenceStatus.SUSPENDED
            LegacyIncidentPreferenceStatus.ACTIVE -> return next == LegacyIncidentPreferenceStatus.SUSPENDED || next == LegacyIncidentPreferenceStatus.CLOSED
            LegacyIncidentPreferenceStatus.SUSPENDED -> return next == LegacyIncidentPreferenceStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LegacyIncidentPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("legacy incident preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun measuredValue(): Double = odometer.toDouble()

    fun snapshot(): LegacyIncidentPreference {
        return LegacyIncidentPreference(
            code = code,
            label = label,
            description = description,
            odometer = odometer,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a legacy incident preference in status draft. */
fun newLegacyIncidentPreference(code: String, label: String): LegacyIncidentPreference {
    return LegacyIncidentPreference(
        code = code,
        label = label,
        description = "",
        odometer = 0,
        timezone = "",
        status = LegacyIncidentPreferenceStatus.DRAFT,
    )
}
