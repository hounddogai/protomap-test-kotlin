package com.acme.transport.crewing.availability.primaryavailabilitytier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual availability draft is in its life cycle. */
enum class ManualAvailabilityDraftStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One manual availability draft, with what the planners need to act on it. */
data class ManualAvailabilityDraft(
    var code: String,
    var title: String,
    var createdAt: Int,
    var contactEmail: String,
    var amount: Double,
    var locale: String,
    var graceDays: Int,
    var maximum: Double,
    var timezone: String,
    var status: ManualAvailabilityDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualAvailabilityDraftStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualAvailabilityDraftStatus): Boolean {
        when (status) {
            ManualAvailabilityDraftStatus.DRAFT -> {
                when (next) {
                    ManualAvailabilityDraftStatus.PENDING, ManualAvailabilityDraftStatus.CLOSED -> return true
                }
                return false
            }
            ManualAvailabilityDraftStatus.PENDING -> {
                when (next) {
                    ManualAvailabilityDraftStatus.ACTIVE -> return true
                }
                return false
            }
            ManualAvailabilityDraftStatus.ACTIVE -> {
                when (next) {
                    ManualAvailabilityDraftStatus.CLOSED -> return true
                }
                return false
            }
            ManualAvailabilityDraftStatus.SUSPENDED -> return next == ManualAvailabilityDraftStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: ManualAvailabilityDraftStatus) {
        if (!canMoveTo(next)) {
            error("manual availability draft $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedTotal(): Double = round((graceDays.toDouble() + maximum) * 100) / 100

    fun snapshot(): ManualAvailabilityDraft {
        return ManualAvailabilityDraft(
            code = code,
            title = title,
            createdAt = createdAt,
            contactEmail = contactEmail,
            amount = amount,
            locale = locale,
            graceDays = graceDays,
            maximum = maximum,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a manual availability draft in status draft. */
fun newManualAvailabilityDraft(code: String, title: String): ManualAvailabilityDraft {
    return ManualAvailabilityDraft(
        code = code,
        title = title,
        createdAt = 0,
        contactEmail = "",
        amount = 0.0,
        locale = "",
        graceDays = 0,
        maximum = 0.0,
        timezone = "",
        status = ManualAvailabilityDraftStatus.DRAFT,
    )
}
