package com.acme.transport.terminal.crossdock.profile

import com.acme.transport.kit.text.normalize

// The legacy cross dock profile records of terminal's cross dock work, and the rules, stores, and
// calculations around them.

/** Where a legacy cross dock profile is in its life cycle. */
enum class LegacyCrossDockProfileStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a legacy cross dock profile and its current status. */
data class LegacyCrossDockProfile(
    var code: String,
    var label: String,
    var amount: Double,
    var contactEmail: String,
    var locale: String,
    var status: LegacyCrossDockProfileStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCrossDockProfileStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCrossDockProfileStatus): Boolean {
        when (status) {
            LegacyCrossDockProfileStatus.DRAFT -> return next == LegacyCrossDockProfileStatus.ACTIVE
            LegacyCrossDockProfileStatus.PENDING -> {
                when (next) {
                    LegacyCrossDockProfileStatus.SUSPENDED -> return true
                }
                return false
            }
            LegacyCrossDockProfileStatus.ACTIVE -> {
                when (next) {
                    LegacyCrossDockProfileStatus.SUSPENDED, LegacyCrossDockProfileStatus.CLOSED -> return true
                }
                return false
            }
            LegacyCrossDockProfileStatus.SUSPENDED -> return next == LegacyCrossDockProfileStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: LegacyCrossDockProfileStatus) {
        if (!canMoveTo(next)) {
            error("legacy cross dock profile $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy cross dock profile"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): LegacyCrossDockProfile {
        return LegacyCrossDockProfile(
            code = code,
            label = label,
            amount = amount,
            contactEmail = contactEmail,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a legacy cross dock profile in status draft. */
fun newLegacyCrossDockProfile(code: String, label: String): LegacyCrossDockProfile {
    return LegacyCrossDockProfile(
        code = code,
        label = label,
        amount = 0.0,
        contactEmail = "",
        locale = "",
        status = LegacyCrossDockProfileStatus.DRAFT,
    )
}
