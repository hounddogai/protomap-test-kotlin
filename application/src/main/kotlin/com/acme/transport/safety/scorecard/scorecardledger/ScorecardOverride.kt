package com.acme.transport.safety.scorecard.scorecardledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard override is in its life cycle. */
enum class ScorecardOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A scorecard override. Its code is unique within a tenant. */
data class ScorecardOverride(
    var code: String,
    var title: String,
    var maximum: Double,
    var archived: Boolean,
    var quantity: Int,
    var graceDays: Int,
    var status: ScorecardOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardOverrideStatus): Boolean {
        when (status) {
            ScorecardOverrideStatus.REQUESTED -> {
                when (next) {
                    ScorecardOverrideStatus.EXPIRED -> return true
                }
                return false
            }
            ScorecardOverrideStatus.GRANTED -> {
                when (next) {
                    ScorecardOverrideStatus.REVOKED, ScorecardOverrideStatus.EXPIRED -> return true
                }
                return false
            }
            ScorecardOverrideStatus.REVOKED -> return next == ScorecardOverrideStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: ScorecardOverrideStatus) {
        if (!canMoveTo(next)) {
            error("scorecard override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scorecard override"} $code"

    fun adjustedTotal(): Double = round(maximum * quantity.toDouble() * 100) / 100

    fun snapshot(): ScorecardOverride {
        return ScorecardOverride(
            code = code,
            title = title,
            maximum = maximum,
            archived = archived,
            quantity = quantity,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scorecard override in status requested. */
fun newScorecardOverride(code: String, title: String): ScorecardOverride {
    return ScorecardOverride(
        code = code,
        title = title,
        maximum = 0.0,
        archived = false,
        quantity = 0,
        graceDays = 0,
        status = ScorecardOverrideStatus.REQUESTED,
    )
}
