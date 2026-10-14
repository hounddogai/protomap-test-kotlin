package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback lane plan summary is in its life cycle. */
enum class FallbackLanePlanSummaryStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A fallback lane plan summary as the lane plan team keeps it. */
data class FallbackLanePlanSummary(
    var code: String,
    var title: String,
    var threshold: Double,
    var channel: String,
    var summary: String,
    var vehicleId: String,
    var odometer: Int,
    var description: String,
    var amount: Double,
    var ratio: Double,
    var unit: String,
    var status: FallbackLanePlanSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackLanePlanSummaryStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackLanePlanSummaryStatus): Boolean {
        when (status) {
            FallbackLanePlanSummaryStatus.NEW -> {
                when (next) {
                    FallbackLanePlanSummaryStatus.RUNNING, FallbackLanePlanSummaryStatus.DONE -> return true
                }
                return false
            }
            FallbackLanePlanSummaryStatus.QUEUED -> {
                when (next) {
                    FallbackLanePlanSummaryStatus.DONE, FallbackLanePlanSummaryStatus.FAILED -> return true
                }
                return false
            }
            FallbackLanePlanSummaryStatus.RUNNING -> return next == FallbackLanePlanSummaryStatus.DONE || next == FallbackLanePlanSummaryStatus.FAILED
            FallbackLanePlanSummaryStatus.DONE -> {
                when (next) {
                    FallbackLanePlanSummaryStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackLanePlanSummaryStatus) {
        if (!canMoveTo(next)) {
            error("fallback lane plan summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedExposure(): Double = round((odometer.toDouble() + threshold) * 100) / 100

    fun snapshot(): FallbackLanePlanSummary {
        return FallbackLanePlanSummary(
            code = code,
            title = title,
            threshold = threshold,
            channel = channel,
            summary = summary,
            vehicleId = vehicleId,
            odometer = odometer,
            description = description,
            amount = amount,
            ratio = ratio,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a fallback lane plan summary in status new. */
fun newFallbackLanePlanSummary(code: String, title: String): FallbackLanePlanSummary {
    return FallbackLanePlanSummary(
        code = code,
        title = title,
        threshold = 0.0,
        channel = "",
        summary = "",
        vehicleId = "",
        odometer = 0,
        description = "",
        amount = 0.0,
        ratio = 0.0,
        unit = "",
        status = FallbackLanePlanSummaryStatus.NEW,
    )
}
