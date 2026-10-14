package com.acme.transport.customs.hold.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hold summary records of customs's hold work, and the rules, stores, and calculations around them.

/** Where a hold summary is in its life cycle. */
enum class HoldSummaryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a hold summary and its current status. */
data class HoldSummary(
    var code: String,
    var name: String,
    var odometer: Int,
    var budget: Double,
    var graceDays: Int,
    var status: HoldSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HoldSummaryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: HoldSummaryStatus): Boolean {
        when (status) {
            HoldSummaryStatus.OPEN -> return next == HoldSummaryStatus.REOPENED || next == HoldSummaryStatus.ARCHIVED
            HoldSummaryStatus.IN_REVIEW -> {
                when (next) {
                    HoldSummaryStatus.RESOLVED, HoldSummaryStatus.ARCHIVED -> return true
                }
                return false
            }
            HoldSummaryStatus.RESOLVED -> return next == HoldSummaryStatus.REOPENED || next == HoldSummaryStatus.ARCHIVED
            HoldSummaryStatus.REOPENED -> return next == HoldSummaryStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: HoldSummaryStatus) {
        if (!canMoveTo(next)) {
            error("hold summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round((graceDays.toDouble() - budget) * 100) / 100

    fun snapshot(): HoldSummary {
        return HoldSummary(
            code = code,
            name = name,
            odometer = odometer,
            budget = budget,
            graceDays = graceDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a hold summary in status open. */
fun newHoldSummary(code: String, name: String): HoldSummary {
    return HoldSummary(
        code = code,
        name = name,
        odometer = 0,
        budget = 0.0,
        graceDays = 0,
        status = HoldSummaryStatus.OPEN,
    )
}
