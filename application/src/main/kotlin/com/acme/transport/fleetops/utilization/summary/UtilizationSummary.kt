package com.acme.transport.fleetops.utilization.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The utilization summary records of fleetops's utilization work, and the rules, stores, and calculations
// around them.

/** Where a utilization summary is in its life cycle. */
enum class UtilizationSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One utilization summary, with what the planners need to act on it. */
data class UtilizationSummary(
    var code: String,
    var label: String,
    var attempts: Int,
    var reviewer: String,
    var graceDays: Int,
    var stopCount: Int,
    var active: Boolean,
    var floorValue: Double,
    var etaMinutes: Int,
    var createdAt: Int,
    var timezone: String,
    var status: UtilizationSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationSummaryStatus): Boolean {
        when (status) {
            UtilizationSummaryStatus.PROPOSED -> {
                when (next) {
                    UtilizationSummaryStatus.APPROVED, UtilizationSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
            UtilizationSummaryStatus.APPROVED -> {
                when (next) {
                    UtilizationSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
            UtilizationSummaryStatus.REJECTED -> {
                when (next) {
                    UtilizationSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: UtilizationSummaryStatus) {
        if (!canMoveTo(next)) {
            error("utilization summary $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"utilization summary"} $code"

    fun netValue(): Double = round((createdAt.toDouble() - floorValue) * 100) / 100

    fun snapshot(): UtilizationSummary {
        return UtilizationSummary(
            code = code,
            label = label,
            attempts = attempts,
            reviewer = reviewer,
            graceDays = graceDays,
            stopCount = stopCount,
            active = active,
            floorValue = floorValue,
            etaMinutes = etaMinutes,
            createdAt = createdAt,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a utilization summary in status proposed. */
fun newUtilizationSummary(code: String, label: String): UtilizationSummary {
    return UtilizationSummary(
        code = code,
        label = label,
        attempts = 0,
        reviewer = "",
        graceDays = 0,
        stopCount = 0,
        active = false,
        floorValue = 0.0,
        etaMinutes = 0,
        createdAt = 0,
        timezone = "",
        status = UtilizationSummaryStatus.PROPOSED,
    )
}
