package com.acme.transport.safety.scorecard.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scorecard window records of safety's scorecard work, and the rules, stores, and calculations around
// them.

/** Where a scorecard window is in its life cycle. */
enum class ScorecardWindowStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scorecard window as the scorecard team keeps it. */
data class ScorecardWindow(
    var code: String,
    var title: String,
    var region: String,
    var trailerId: String,
    var locale: String,
    var graceDays: Int,
    var dock: String,
    var discount: Double,
    var status: ScorecardWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardWindowStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardWindowStatus): Boolean {
        when (status) {
            ScorecardWindowStatus.OPEN -> {
                when (next) {
                    ScorecardWindowStatus.RESOLVED, ScorecardWindowStatus.ARCHIVED -> return true
                }
                return false
            }
            ScorecardWindowStatus.IN_REVIEW -> return next == ScorecardWindowStatus.ARCHIVED
            ScorecardWindowStatus.RESOLVED -> {
                when (next) {
                    ScorecardWindowStatus.REOPENED -> return true
                }
                return false
            }
            ScorecardWindowStatus.REOPENED -> {
                when (next) {
                    ScorecardWindowStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScorecardWindowStatus) {
        if (!canMoveTo(next)) {
            error("scorecard window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedExposure(): Double = round(graceDays.toDouble() * discount * 100) / 100

    fun snapshot(): ScorecardWindow {
        return ScorecardWindow(
            code = code,
            title = title,
            region = region,
            trailerId = trailerId,
            locale = locale,
            graceDays = graceDays,
            dock = dock,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scorecard window in status open. */
fun newScorecardWindow(code: String, title: String): ScorecardWindow {
    return ScorecardWindow(
        code = code,
        title = title,
        region = "",
        trailerId = "",
        locale = "",
        graceDays = 0,
        dock = "",
        discount = 0.0,
        status = ScorecardWindowStatus.OPEN,
    )
}
