package com.acme.transport.safety.violation.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The primary violation summary records of safety's violation work, and the rules, stores, and calculations
// around them.

/** Where a primary violation summary is in its life cycle. */
enum class PrimaryViolationSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One primary violation summary, with what the planners need to act on it. */
data class PrimaryViolationSummary(
    var code: String,
    var label: String,
    var ceiling: Double,
    var timezone: String,
    var stopCount: Int,
    var source: String,
    var country: String,
    var cost: Double,
    var vehicleId: String,
    var visible: Boolean,
    var status: PrimaryViolationSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryViolationSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryViolationSummaryStatus): Boolean {
        when (status) {
            PrimaryViolationSummaryStatus.PROPOSED -> return next == PrimaryViolationSummaryStatus.APPROVED
            PrimaryViolationSummaryStatus.APPROVED -> {
                when (next) {
                    PrimaryViolationSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
            PrimaryViolationSummaryStatus.REJECTED -> {
                when (next) {
                    PrimaryViolationSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryViolationSummaryStatus) {
        if (!canMoveTo(next)) {
            error("primary violation summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($label)"

    fun projectedValue(): Double = round(stopCount.toDouble() * cost * 100) / 100

    fun snapshot(): PrimaryViolationSummary {
        return PrimaryViolationSummary(
            code = code,
            label = label,
            ceiling = ceiling,
            timezone = timezone,
            stopCount = stopCount,
            source = source,
            country = country,
            cost = cost,
            vehicleId = vehicleId,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a primary violation summary in status proposed. */
fun newPrimaryViolationSummary(code: String, label: String): PrimaryViolationSummary {
    return PrimaryViolationSummary(
        code = code,
        label = label,
        ceiling = 0.0,
        timezone = "",
        stopCount = 0,
        source = "",
        country = "",
        cost = 0.0,
        vehicleId = "",
        visible = false,
        status = PrimaryViolationSummaryStatus.PROPOSED,
    )
}
