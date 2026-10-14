package com.acme.transport.fleetops.utilization.utilizationsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The adaptive utilization summary records of fleetops's utilization work, and the rules, stores, and
// calculations around them.

/** Where a adaptive utilization summary is in its life cycle. */
enum class AdaptiveUtilizationSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a adaptive utilization summary and its current status. */
data class AdaptiveUtilizationSummary(
    var code: String,
    var name: String,
    var endsAt: Int,
    var amount: Double,
    var floorValue: Double,
    var discount: Double,
    var status: AdaptiveUtilizationSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveUtilizationSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveUtilizationSummaryStatus): Boolean {
        when (status) {
            AdaptiveUtilizationSummaryStatus.PLANNED -> return next == AdaptiveUtilizationSummaryStatus.SCHEDULED
            AdaptiveUtilizationSummaryStatus.SCHEDULED -> {
                when (next) {
                    AdaptiveUtilizationSummaryStatus.IN_PROGRESS, AdaptiveUtilizationSummaryStatus.COMPLETED -> return true
                }
                return false
            }
            AdaptiveUtilizationSummaryStatus.IN_PROGRESS -> return next == AdaptiveUtilizationSummaryStatus.COMPLETED || next == AdaptiveUtilizationSummaryStatus.CANCELLED
            AdaptiveUtilizationSummaryStatus.COMPLETED -> return next == AdaptiveUtilizationSummaryStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: AdaptiveUtilizationSummaryStatus) {
        if (!canMoveTo(next)) {
            error("adaptive utilization summary $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$name]"

    fun netVolume(): Double = round((discount - endsAt.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveUtilizationSummary {
        return AdaptiveUtilizationSummary(
            code = code,
            name = name,
            endsAt = endsAt,
            amount = amount,
            floorValue = floorValue,
            discount = discount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a adaptive utilization summary in status planned. */
fun newAdaptiveUtilizationSummary(code: String, name: String): AdaptiveUtilizationSummary {
    return AdaptiveUtilizationSummary(
        code = code,
        name = name,
        endsAt = 0,
        amount = 0.0,
        floorValue = 0.0,
        discount = 0.0,
        status = AdaptiveUtilizationSummaryStatus.PLANNED,
    )
}
