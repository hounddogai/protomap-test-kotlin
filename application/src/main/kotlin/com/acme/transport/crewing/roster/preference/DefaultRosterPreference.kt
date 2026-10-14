package com.acme.transport.crewing.roster.preference

import com.acme.transport.kit.text.normalize

// The default roster preference records of crewing's roster work, and the rules, stores, and calculations
// around them.

/** Where a default roster preference is in its life cycle. */
enum class DefaultRosterPreferenceStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a default roster preference and its current status. */
data class DefaultRosterPreference(
    var code: String,
    var title: String,
    var phone: String,
    var category: String,
    var contactEmail: String,
    var carrier: String,
    var ceiling: Double,
    var status: DefaultRosterPreferenceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DefaultRosterPreferenceStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: DefaultRosterPreferenceStatus): Boolean {
        when (status) {
            DefaultRosterPreferenceStatus.OPEN -> {
                when (next) {
                    DefaultRosterPreferenceStatus.IN_REVIEW, DefaultRosterPreferenceStatus.ARCHIVED -> return true
                }
                return false
            }
            DefaultRosterPreferenceStatus.IN_REVIEW -> {
                when (next) {
                    DefaultRosterPreferenceStatus.RESOLVED, DefaultRosterPreferenceStatus.REOPENED -> return true
                }
                return false
            }
            DefaultRosterPreferenceStatus.RESOLVED -> return next == DefaultRosterPreferenceStatus.REOPENED || next == DefaultRosterPreferenceStatus.ARCHIVED
            DefaultRosterPreferenceStatus.REOPENED -> {
                when (next) {
                    DefaultRosterPreferenceStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DefaultRosterPreferenceStatus) {
        if (!canMoveTo(next)) {
            error("default roster preference $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = ceiling

    fun snapshot(): DefaultRosterPreference {
        return DefaultRosterPreference(
            code = code,
            title = title,
            phone = phone,
            category = category,
            contactEmail = contactEmail,
            carrier = carrier,
            ceiling = ceiling,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a default roster preference in status open. */
fun newDefaultRosterPreference(code: String, title: String): DefaultRosterPreference {
    return DefaultRosterPreference(
        code = code,
        title = title,
        phone = "",
        category = "",
        contactEmail = "",
        carrier = "",
        ceiling = 0.0,
        status = DefaultRosterPreferenceStatus.OPEN,
    )
}
