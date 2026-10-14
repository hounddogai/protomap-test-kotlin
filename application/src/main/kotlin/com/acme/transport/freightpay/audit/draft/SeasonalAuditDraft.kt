package com.acme.transport.freightpay.audit.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal audit draft records of freightpay's audit work, and the rules, stores, and calculations around
// them.

/** Where a seasonal audit draft is in its life cycle. */
enum class SeasonalAuditDraftStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One seasonal audit draft, with what the planners need to act on it. */
data class SeasonalAuditDraft(
    var code: String,
    var label: String,
    var country: String,
    var durationMinutes: Int,
    var discount: Double,
    var status: SeasonalAuditDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalAuditDraftStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalAuditDraftStatus): Boolean {
        when (status) {
            SeasonalAuditDraftStatus.IDLE -> {
                when (next) {
                    SeasonalAuditDraftStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalAuditDraftStatus.WARMING -> return next == SeasonalAuditDraftStatus.STOPPED
            SeasonalAuditDraftStatus.READY -> {
                when (next) {
                    SeasonalAuditDraftStatus.DRAINING, SeasonalAuditDraftStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalAuditDraftStatus.DRAINING -> {
                when (next) {
                    SeasonalAuditDraftStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalAuditDraftStatus) {
        if (!canMoveTo(next)) {
            error("seasonal audit draft $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveValue(): Double = round((durationMinutes.toDouble() + discount) * 100) / 100

    fun snapshot(): SeasonalAuditDraft {
        return SeasonalAuditDraft(
            code = code,
            label = label,
            country = country,
            durationMinutes = durationMinutes,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a seasonal audit draft in status idle. */
fun newSeasonalAuditDraft(code: String, label: String): SeasonalAuditDraft {
    return SeasonalAuditDraft(
        code = code,
        label = label,
        country = "",
        durationMinutes = 0,
        discount = 0.0,
        status = SeasonalAuditDraftStatus.IDLE,
    )
}
