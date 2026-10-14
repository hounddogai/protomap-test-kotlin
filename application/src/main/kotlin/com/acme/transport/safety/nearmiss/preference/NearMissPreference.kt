package com.acme.transport.safety.nearmiss.preference

import com.acme.transport.kit.text.normalize

// The near miss preference records of safety's near miss work, and the rules, stores, and calculations around
// them.

/** Where a near miss preference is in its life cycle. */
enum class NearMissPreferenceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A near miss preference. Its code is unique within a tenant. */
data class NearMissPreference(
    var code: String,
    var label: String,
    var depot: String,
    var attributes: MutableMap<String, String>,
    var maximum: Double,
    var status: NearMissPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            NearMissPreferenceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: NearMissPreferenceStatus): Boolean {
        when (status) {
            NearMissPreferenceStatus.PLANNED -> return next == NearMissPreferenceStatus.COMPLETED
            NearMissPreferenceStatus.SCHEDULED -> return next == NearMissPreferenceStatus.IN_PROGRESS || next == NearMissPreferenceStatus.CANCELLED
            NearMissPreferenceStatus.IN_PROGRESS -> {
                when (next) {
                    NearMissPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
            NearMissPreferenceStatus.COMPLETED -> return next == NearMissPreferenceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: NearMissPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("near miss preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"near miss preference"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun measuredValue(): Double = maximum

    fun snapshot(): NearMissPreference {
        return NearMissPreference(
            code = code,
            label = label,
            depot = depot,
            attributes = attributes,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a near miss preference in status planned. */
fun newNearMissPreference(code: String, label: String): NearMissPreference {
    return NearMissPreference(
        code = code,
        label = label,
        depot = "",
        attributes = mutableMapOf<String, String>(),
        maximum = 0.0,
        status = NearMissPreferenceStatus.PLANNED,
    )
}
