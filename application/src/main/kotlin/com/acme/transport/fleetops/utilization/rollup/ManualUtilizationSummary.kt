package com.acme.transport.fleetops.utilization.rollup

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a manual utilization summary is in its life cycle. */
enum class ManualUtilizationSummaryStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One manual utilization summary, with what the planners need to act on it. */
data class ManualUtilizationSummary(
    var code: String,
    var name: String,
    var trailerId: String,
    var startsAt: Int,
    var timezone: String,
    var odometer: Int,
    var region: String,
    var status: ManualUtilizationSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ManualUtilizationSummaryStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: ManualUtilizationSummaryStatus): Boolean {
        when (status) {
            ManualUtilizationSummaryStatus.DRAFT -> {
                when (next) {
                    ManualUtilizationSummaryStatus.SUSPENDED -> return true
                }
                return false
            }
            ManualUtilizationSummaryStatus.PENDING -> {
                when (next) {
                    ManualUtilizationSummaryStatus.ACTIVE -> return true
                }
                return false
            }
            ManualUtilizationSummaryStatus.ACTIVE -> {
                when (next) {
                    ManualUtilizationSummaryStatus.CLOSED -> return true
                }
                return false
            }
            ManualUtilizationSummaryStatus.SUSPENDED -> {
                when (next) {
                    ManualUtilizationSummaryStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ManualUtilizationSummaryStatus) {
        if (!canMoveTo(next)) {
            error("manual utilization summary $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedExposure(): Double = round((startsAt.toDouble() - odometer.toDouble()) * 100) / 100

    fun snapshot(): ManualUtilizationSummary {
        return ManualUtilizationSummary(
            code = code,
            name = name,
            trailerId = trailerId,
            startsAt = startsAt,
            timezone = timezone,
            odometer = odometer,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a manual utilization summary in status draft. */
fun newManualUtilizationSummary(code: String, name: String): ManualUtilizationSummary {
    return ManualUtilizationSummary(
        code = code,
        name = name,
        trailerId = "",
        startsAt = 0,
        timezone = "",
        odometer = 0,
        region = "",
        status = ManualUtilizationSummaryStatus.DRAFT,
    )
}
