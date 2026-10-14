package com.acme.transport.terminal.dwell.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted dwell summary records of terminal's dwell work, and the rules, stores, and calculations around
// them.

/** Where a weighted dwell summary is in its life cycle. */
enum class WeightedDwellSummaryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One weighted dwell summary, with what the planners need to act on it. */
data class WeightedDwellSummary(
    var code: String,
    var label: String,
    var retries: Int,
    var trailerId: String,
    var pallets: Int,
    var updatedAt: Int,
    var expiresAt: Int,
    var etaMinutes: Int,
    var status: WeightedDwellSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedDwellSummaryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedDwellSummaryStatus): Boolean {
        when (status) {
            WeightedDwellSummaryStatus.OPEN -> return next == WeightedDwellSummaryStatus.RESOLVED || next == WeightedDwellSummaryStatus.ARCHIVED
            WeightedDwellSummaryStatus.IN_REVIEW -> {
                when (next) {
                    WeightedDwellSummaryStatus.REOPENED, WeightedDwellSummaryStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedDwellSummaryStatus.RESOLVED -> {
                when (next) {
                    WeightedDwellSummaryStatus.REOPENED -> return true
                }
                return false
            }
            WeightedDwellSummaryStatus.REOPENED -> return next == WeightedDwellSummaryStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: WeightedDwellSummaryStatus) {
        if (!canMoveTo(next)) {
            error("weighted dwell summary $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"weighted dwell summary"} $code"

    fun effectiveTotal(): Double = round(updatedAt.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): WeightedDwellSummary {
        return WeightedDwellSummary(
            code = code,
            label = label,
            retries = retries,
            trailerId = trailerId,
            pallets = pallets,
            updatedAt = updatedAt,
            expiresAt = expiresAt,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted dwell summary in status open. */
fun newWeightedDwellSummary(code: String, label: String): WeightedDwellSummary {
    return WeightedDwellSummary(
        code = code,
        label = label,
        retries = 0,
        trailerId = "",
        pallets = 0,
        updatedAt = 0,
        expiresAt = 0,
        etaMinutes = 0,
        status = WeightedDwellSummaryStatus.OPEN,
    )
}
