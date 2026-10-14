package com.acme.transport.terminal.crossdock.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The legacy cross dock summary records of terminal's cross dock work, and the rules, stores, and
// calculations around them.

/** Where a legacy cross dock summary is in its life cycle. */
enum class LegacyCrossDockSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A legacy cross dock summary as the cross dock team keeps it. */
data class LegacyCrossDockSummary(
    var code: String,
    var title: String,
    var lane: String,
    var margin: Double,
    var priority: Int,
    var label: String,
    var limit: Int,
    var loadKg: Double,
    var pallets: Int,
    var retries: Int,
    var trailerId: String,
    var status: LegacyCrossDockSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LegacyCrossDockSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LegacyCrossDockSummaryStatus): Boolean {
        when (status) {
            LegacyCrossDockSummaryStatus.PROPOSED -> {
                when (next) {
                    LegacyCrossDockSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyCrossDockSummaryStatus.APPROVED -> {
                when (next) {
                    LegacyCrossDockSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
            LegacyCrossDockSummaryStatus.REJECTED -> return next == LegacyCrossDockSummaryStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LegacyCrossDockSummaryStatus) {
        if (!canMoveTo(next)) {
            error("legacy cross dock summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"legacy cross dock summary"} $code"

    fun weightedVolume(): Double = round((priority.toDouble() + limit.toDouble()) * 100) / 100

    fun snapshot(): LegacyCrossDockSummary {
        return LegacyCrossDockSummary(
            code = code,
            title = title,
            lane = lane,
            margin = margin,
            priority = priority,
            label = label,
            limit = limit,
            loadKg = loadKg,
            pallets = pallets,
            retries = retries,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a legacy cross dock summary in status proposed. */
fun newLegacyCrossDockSummary(code: String, title: String): LegacyCrossDockSummary {
    return LegacyCrossDockSummary(
        code = code,
        title = title,
        lane = "",
        margin = 0.0,
        priority = 0,
        label = "",
        limit = 0,
        loadKg = 0.0,
        pallets = 0,
        retries = 0,
        trailerId = "",
        status = LegacyCrossDockSummaryStatus.PROPOSED,
    )
}
