package com.acme.transport.safety.scorecard.scorecardslot

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The fallback scorecard slot records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a fallback scorecard slot is in its life cycle. */
enum class FallbackScorecardSlotStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a fallback scorecard slot and its current status. */
data class FallbackScorecardSlot(
    var code: String,
    var title: String,
    var startsAt: Int,
    var currency: String,
    var batchSize: Int,
    var contactEmail: String,
    var durationMinutes: Int,
    var country: String,
    var timezone: String,
    var status: FallbackScorecardSlotStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackScorecardSlotStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackScorecardSlotStatus): Boolean {
        when (status) {
            FallbackScorecardSlotStatus.OPEN -> return next == FallbackScorecardSlotStatus.IN_REVIEW || next == FallbackScorecardSlotStatus.REOPENED
            FallbackScorecardSlotStatus.IN_REVIEW -> return next == FallbackScorecardSlotStatus.RESOLVED || next == FallbackScorecardSlotStatus.REOPENED
            FallbackScorecardSlotStatus.RESOLVED -> return next == FallbackScorecardSlotStatus.ARCHIVED
            FallbackScorecardSlotStatus.REOPENED -> return next == FallbackScorecardSlotStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: FallbackScorecardSlotStatus) {
        if (!canMoveTo(next)) {
            error("fallback scorecard slot $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $title"

    fun projectedLoad(): Double = round((durationMinutes.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): FallbackScorecardSlot {
        return FallbackScorecardSlot(
            code = code,
            title = title,
            startsAt = startsAt,
            currency = currency,
            batchSize = batchSize,
            contactEmail = contactEmail,
            durationMinutes = durationMinutes,
            country = country,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a fallback scorecard slot in status open. */
fun newFallbackScorecardSlot(code: String, title: String): FallbackScorecardSlot {
    return FallbackScorecardSlot(
        code = code,
        title = title,
        startsAt = 0,
        currency = "",
        batchSize = 0,
        contactEmail = "",
        durationMinutes = 0,
        country = "",
        timezone = "",
        status = FallbackScorecardSlotStatus.OPEN,
    )
}
