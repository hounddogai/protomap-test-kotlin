package com.acme.transport.freightpay.ratecard.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rate card draft records of freightpay's rate card work, and the rules, stores, and calculations around
// them.

/** Where a rate card draft is in its life cycle. */
enum class RateCardDraftStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A rate card draft as the rate card team keeps it. */
data class RateCardDraft(
    var code: String,
    var label: String,
    var stopCount: Int,
    var graceDays: Int,
    var shiftCode: String,
    var createdAt: Int,
    var retries: Int,
    var score: Double,
    var depot: String,
    var durationMinutes: Int,
    var status: RateCardDraftStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RateCardDraftStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RateCardDraftStatus): Boolean {
        when (status) {
            RateCardDraftStatus.PLANNED -> {
                when (next) {
                    RateCardDraftStatus.SCHEDULED, RateCardDraftStatus.CANCELLED -> return true
                }
                return false
            }
            RateCardDraftStatus.SCHEDULED -> {
                when (next) {
                    RateCardDraftStatus.IN_PROGRESS, RateCardDraftStatus.CANCELLED -> return true
                }
                return false
            }
            RateCardDraftStatus.IN_PROGRESS -> {
                when (next) {
                    RateCardDraftStatus.CANCELLED -> return true
                }
                return false
            }
            RateCardDraftStatus.COMPLETED -> return next == RateCardDraftStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RateCardDraftStatus) {
        if (!canMoveTo(next)) {
            error("rate card draft $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveVolume(): Double = round(graceDays.toDouble() * stopCount.toDouble() * 100) / 100

    fun snapshot(): RateCardDraft {
        return RateCardDraft(
            code = code,
            label = label,
            stopCount = stopCount,
            graceDays = graceDays,
            shiftCode = shiftCode,
            createdAt = createdAt,
            retries = retries,
            score = score,
            depot = depot,
            durationMinutes = durationMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rate card draft in status planned. */
fun newRateCardDraft(code: String, label: String): RateCardDraft {
    return RateCardDraft(
        code = code,
        label = label,
        stopCount = 0,
        graceDays = 0,
        shiftCode = "",
        createdAt = 0,
        retries = 0,
        score = 0.0,
        depot = "",
        durationMinutes = 0,
        status = RateCardDraftStatus.PLANNED,
    )
}
