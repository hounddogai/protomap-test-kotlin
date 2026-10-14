package com.acme.transport.safety.incident.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The strict incident preference records of safety's incident work, and the rules, stores, and calculations
// around them.

/** Where a strict incident preference is in its life cycle. */
enum class StrictIncidentPreferenceStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A strict incident preference. Its code is unique within a tenant. */
data class StrictIncidentPreference(
    var code: String,
    var name: String,
    var category: String,
    var region: String,
    var threshold: Double,
    var graceDays: Int,
    var approved: Boolean,
    var depot: String,
    var distanceKm: Double,
    var capacity: Int,
    var status: StrictIncidentPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictIncidentPreferenceStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictIncidentPreferenceStatus): Boolean {
        when (status) {
            StrictIncidentPreferenceStatus.PROPOSED -> return next == StrictIncidentPreferenceStatus.APPROVED || next == StrictIncidentPreferenceStatus.REJECTED
            StrictIncidentPreferenceStatus.APPROVED -> return next == StrictIncidentPreferenceStatus.REJECTED
            StrictIncidentPreferenceStatus.REJECTED -> return next == StrictIncidentPreferenceStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: StrictIncidentPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("strict incident preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedValue(): Double = round((graceDays.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): StrictIncidentPreference {
        return StrictIncidentPreference(
            code = code,
            name = name,
            category = category,
            region = region,
            threshold = threshold,
            graceDays = graceDays,
            approved = approved,
            depot = depot,
            distanceKm = distanceKm,
            capacity = capacity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a strict incident preference in status proposed. */
fun newStrictIncidentPreference(code: String, name: String): StrictIncidentPreference {
    return StrictIncidentPreference(
        code = code,
        name = name,
        category = "",
        region = "",
        threshold = 0.0,
        graceDays = 0,
        approved = false,
        depot = "",
        distanceKm = 0.0,
        capacity = 0,
        status = StrictIncidentPreferenceStatus.PROPOSED,
    )
}
