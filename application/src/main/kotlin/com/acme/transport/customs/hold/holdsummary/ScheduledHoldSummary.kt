package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled hold summary records of customs's hold work, and the rules, stores, and calculations around
// them.

/** Where a scheduled hold summary is in its life cycle. */
enum class ScheduledHoldSummaryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a scheduled hold summary and its current status. */
data class ScheduledHoldSummary(
    var code: String,
    var name: String,
    var depot: String,
    var capacity: Int,
    var distanceKm: Double,
    var country: String,
    var timezone: String,
    var budget: Double,
    var pallets: Int,
    var dock: String,
    var minimum: Double,
    var status: ScheduledHoldSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledHoldSummaryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledHoldSummaryStatus): Boolean {
        when (status) {
            ScheduledHoldSummaryStatus.OPEN -> {
                when (next) {
                    ScheduledHoldSummaryStatus.IN_REVIEW -> return true
                }
                return false
            }
            ScheduledHoldSummaryStatus.IN_REVIEW -> return next == ScheduledHoldSummaryStatus.RESOLVED || next == ScheduledHoldSummaryStatus.ARCHIVED
            ScheduledHoldSummaryStatus.RESOLVED -> return next == ScheduledHoldSummaryStatus.ARCHIVED
            ScheduledHoldSummaryStatus.REOPENED -> {
                when (next) {
                    ScheduledHoldSummaryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledHoldSummaryStatus) {
        if (!canMoveTo(next)) {
            error("scheduled hold summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedExposure(): Double = round((pallets.toDouble() - budget) * 100) / 100

    fun snapshot(): ScheduledHoldSummary {
        return ScheduledHoldSummary(
            code = code,
            name = name,
            depot = depot,
            capacity = capacity,
            distanceKm = distanceKm,
            country = country,
            timezone = timezone,
            budget = budget,
            pallets = pallets,
            dock = dock,
            minimum = minimum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a scheduled hold summary in status open. */
fun newScheduledHoldSummary(code: String, name: String): ScheduledHoldSummary {
    return ScheduledHoldSummary(
        code = code,
        name = name,
        depot = "",
        capacity = 0,
        distanceKm = 0.0,
        country = "",
        timezone = "",
        budget = 0.0,
        pallets = 0,
        dock = "",
        minimum = 0.0,
        status = ScheduledHoldSummaryStatus.OPEN,
    )
}
