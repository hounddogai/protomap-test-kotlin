package com.acme.transport.network.transittime.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled transit time summary records of network's transit time work, and the rules, stores, and
// calculations around them.

/** Where a scheduled transit time summary is in its life cycle. */
enum class ScheduledTransitTimeSummaryStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A scheduled transit time summary as the transit time team keeps it. */
data class ScheduledTransitTimeSummary(
    var code: String,
    var name: String,
    var budget: Double,
    var updatedAt: Int,
    var depot: String,
    var source: String,
    var expiresAt: Int,
    var status: ScheduledTransitTimeSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledTransitTimeSummaryStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledTransitTimeSummaryStatus): Boolean {
        when (status) {
            ScheduledTransitTimeSummaryStatus.CREATED -> return next == ScheduledTransitTimeSummaryStatus.VALIDATED || next == ScheduledTransitTimeSummaryStatus.PUBLISHED
            ScheduledTransitTimeSummaryStatus.VALIDATED -> {
                when (next) {
                    ScheduledTransitTimeSummaryStatus.RETIRED -> return true
                }
                return false
            }
            ScheduledTransitTimeSummaryStatus.PUBLISHED -> return next == ScheduledTransitTimeSummaryStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledTransitTimeSummaryStatus) {
        if (!canMoveTo(next)) {
            error("scheduled transit time summary $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun effectiveLoad(): Double = round(updatedAt.toDouble() * expiresAt.toDouble() * 100) / 100

    fun snapshot(): ScheduledTransitTimeSummary {
        return ScheduledTransitTimeSummary(
            code = code,
            name = name,
            budget = budget,
            updatedAt = updatedAt,
            depot = depot,
            source = source,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scheduled transit time summary in status created. */
fun newScheduledTransitTimeSummary(code: String, name: String): ScheduledTransitTimeSummary {
    return ScheduledTransitTimeSummary(
        code = code,
        name = name,
        budget = 0.0,
        updatedAt = 0,
        depot = "",
        source = "",
        expiresAt = 0,
        status = ScheduledTransitTimeSummaryStatus.CREATED,
    )
}
