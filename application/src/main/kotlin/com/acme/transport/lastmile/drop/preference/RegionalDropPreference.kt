package com.acme.transport.lastmile.drop.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional drop preference records of lastmile's drop work, and the rules, stores, and calculations
// around them.

/** Where a regional drop preference is in its life cycle. */
enum class RegionalDropPreferenceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One regional drop preference, with what the planners need to act on it. */
data class RegionalDropPreference(
    var code: String,
    var label: String,
    var limit: Int,
    var contactEmail: String,
    var amount: Double,
    var attributes: MutableMap<String, String>,
    var visible: Boolean,
    var expiresAt: Int,
    var status: RegionalDropPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDropPreferenceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDropPreferenceStatus): Boolean {
        when (status) {
            RegionalDropPreferenceStatus.PLANNED -> {
                when (next) {
                    RegionalDropPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalDropPreferenceStatus.SCHEDULED -> {
                when (next) {
                    RegionalDropPreferenceStatus.IN_PROGRESS, RegionalDropPreferenceStatus.COMPLETED -> return true
                }
                return false
            }
            RegionalDropPreferenceStatus.IN_PROGRESS -> return next == RegionalDropPreferenceStatus.COMPLETED || next == RegionalDropPreferenceStatus.CANCELLED
            RegionalDropPreferenceStatus.COMPLETED -> {
                when (next) {
                    RegionalDropPreferenceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDropPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("regional drop preference $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun projectedTotal(): Double = round((limit.toDouble() - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): RegionalDropPreference {
        return RegionalDropPreference(
            code = code,
            label = label,
            limit = limit,
            contactEmail = contactEmail,
            amount = amount,
            attributes = attributes,
            visible = visible,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional drop preference in status planned. */
fun newRegionalDropPreference(code: String, label: String): RegionalDropPreference {
    return RegionalDropPreference(
        code = code,
        label = label,
        limit = 0,
        contactEmail = "",
        amount = 0.0,
        attributes = mutableMapOf<String, String>(),
        visible = false,
        expiresAt = 0,
        status = RegionalDropPreferenceStatus.PLANNED,
    )
}
