package com.acme.transport.safety.scorecard.defaultscorecardledger

import com.acme.transport.kit.text.normalize

/** Where a scorecard adjustment is in its life cycle. */
enum class ScorecardAdjustmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a scorecard adjustment and its current status. */
data class ScorecardAdjustment(
    var code: String,
    var title: String,
    var approved: Boolean,
    var trailerId: String,
    var discount: Double,
    var status: ScorecardAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardAdjustmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardAdjustmentStatus): Boolean {
        when (status) {
            ScorecardAdjustmentStatus.CREATED -> {
                when (next) {
                    ScorecardAdjustmentStatus.VALIDATED, ScorecardAdjustmentStatus.PUBLISHED -> return true
                }
                return false
            }
            ScorecardAdjustmentStatus.VALIDATED -> return next == ScorecardAdjustmentStatus.RETIRED
            ScorecardAdjustmentStatus.PUBLISHED -> {
                when (next) {
                    ScorecardAdjustmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScorecardAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("scorecard adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun measuredValue(): Double = discount

    fun snapshot(): ScorecardAdjustment {
        return ScorecardAdjustment(
            code = code,
            title = title,
            approved = approved,
            trailerId = trailerId,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a scorecard adjustment in status created. */
fun newScorecardAdjustment(code: String, title: String): ScorecardAdjustment {
    return ScorecardAdjustment(
        code = code,
        title = title,
        approved = false,
        trailerId = "",
        discount = 0.0,
        status = ScorecardAdjustmentStatus.CREATED,
    )
}
