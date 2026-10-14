package com.acme.transport.terminal.dockdoor.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional dock door metric is in its life cycle. */
enum class RegionalDockDoorMetricStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One regional dock door metric, with what the planners need to act on it. */
data class RegionalDockDoorMetric(
    var code: String,
    var title: String,
    var attempts: Int,
    var timezone: String,
    var budget: Double,
    var amount: Double,
    var country: String,
    var status: RegionalDockDoorMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDockDoorMetricStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDockDoorMetricStatus): Boolean {
        when (status) {
            RegionalDockDoorMetricStatus.IDLE -> {
                when (next) {
                    RegionalDockDoorMetricStatus.READY -> return true
                }
                return false
            }
            RegionalDockDoorMetricStatus.WARMING -> return next == RegionalDockDoorMetricStatus.READY || next == RegionalDockDoorMetricStatus.STOPPED
            RegionalDockDoorMetricStatus.READY -> return next == RegionalDockDoorMetricStatus.DRAINING
            RegionalDockDoorMetricStatus.DRAINING -> return next == RegionalDockDoorMetricStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: RegionalDockDoorMetricStatus) {
        if (!canMoveTo(next)) {
            error("regional dock door metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun weightedValue(): Double = round(attempts.toDouble() * amount * 100) / 100

    fun snapshot(): RegionalDockDoorMetric {
        return RegionalDockDoorMetric(
            code = code,
            title = title,
            attempts = attempts,
            timezone = timezone,
            budget = budget,
            amount = amount,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a regional dock door metric in status idle. */
fun newRegionalDockDoorMetric(code: String, title: String): RegionalDockDoorMetric {
    return RegionalDockDoorMetric(
        code = code,
        title = title,
        attempts = 0,
        timezone = "",
        budget = 0.0,
        amount = 0.0,
        country = "",
        status = RegionalDockDoorMetricStatus.IDLE,
    )
}
