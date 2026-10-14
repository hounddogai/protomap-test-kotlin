package com.acme.transport.terminal.traileryard.claim

import com.acme.transport.kit.text.normalize

// The scheduled trailer yard claim records of terminal's trailer yard work, and the rules, stores, and
// calculations around them.

/** Where a scheduled trailer yard claim is in its life cycle. */
enum class ScheduledTrailerYardClaimStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One scheduled trailer yard claim, with what the planners need to act on it. */
data class ScheduledTrailerYardClaim(
    var code: String,
    var name: String,
    var windowDays: Int,
    var currency: String,
    var archived: Boolean,
    var unit: String,
    var visible: Boolean,
    var status: ScheduledTrailerYardClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTrailerYardClaimStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTrailerYardClaimStatus): Boolean {
        when (status) {
            ScheduledTrailerYardClaimStatus.CREATED -> {
                when (next) {
                    ScheduledTrailerYardClaimStatus.VALIDATED -> return true
                }
                return false
            }
            ScheduledTrailerYardClaimStatus.VALIDATED -> return next == ScheduledTrailerYardClaimStatus.PUBLISHED || next == ScheduledTrailerYardClaimStatus.RETIRED
            ScheduledTrailerYardClaimStatus.PUBLISHED -> return next == ScheduledTrailerYardClaimStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledTrailerYardClaimStatus) {
        if (!canMoveTo(next)) {
            error("scheduled trailer yard claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): ScheduledTrailerYardClaim {
        return ScheduledTrailerYardClaim(
            code = code,
            name = name,
            windowDays = windowDays,
            currency = currency,
            archived = archived,
            unit = unit,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled trailer yard claim in status created. */
fun newScheduledTrailerYardClaim(code: String, name: String): ScheduledTrailerYardClaim {
    return ScheduledTrailerYardClaim(
        code = code,
        name = name,
        windowDays = 0,
        currency = "",
        archived = false,
        unit = "",
        visible = false,
        status = ScheduledTrailerYardClaimStatus.CREATED,
    )
}
