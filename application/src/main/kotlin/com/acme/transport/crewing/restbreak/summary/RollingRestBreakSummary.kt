package com.acme.transport.crewing.restbreak.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The rolling rest break summary records of crewing's rest break work, and the rules, stores, and
// calculations around them.

/** Where a rolling rest break summary is in its life cycle. */
enum class RollingRestBreakSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One rolling rest break summary, with what the planners need to act on it. */
data class RollingRestBreakSummary(
    var code: String,
    var name: String,
    var quantity: Int,
    var floorValue: Double,
    var rate: Double,
    var weight: Double,
    var status: RollingRestBreakSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingRestBreakSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingRestBreakSummaryStatus): Boolean {
        when (status) {
            RollingRestBreakSummaryStatus.PLANNED -> {
                when (next) {
                    RollingRestBreakSummaryStatus.CANCELLED -> return true
                }
                return false
            }
            RollingRestBreakSummaryStatus.SCHEDULED -> return next == RollingRestBreakSummaryStatus.COMPLETED
            RollingRestBreakSummaryStatus.IN_PROGRESS -> {
                when (next) {
                    RollingRestBreakSummaryStatus.COMPLETED, RollingRestBreakSummaryStatus.CANCELLED -> return true
                }
                return false
            }
            RollingRestBreakSummaryStatus.COMPLETED -> return next == RollingRestBreakSummaryStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: RollingRestBreakSummaryStatus) {
        if (!canMoveTo(next)) {
            error("rolling rest break summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun weightedLoad(): Double = round(rate * quantity.toDouble() * 100) / 100

    fun snapshot(): RollingRestBreakSummary {
        return RollingRestBreakSummary(
            code = code,
            name = name,
            quantity = quantity,
            floorValue = floorValue,
            rate = rate,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a rolling rest break summary in status planned. */
fun newRollingRestBreakSummary(code: String, name: String): RollingRestBreakSummary {
    return RollingRestBreakSummary(
        code = code,
        name = name,
        quantity = 0,
        floorValue = 0.0,
        rate = 0.0,
        weight = 0.0,
        status = RollingRestBreakSummaryStatus.PLANNED,
    )
}
