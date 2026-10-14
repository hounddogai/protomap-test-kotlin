package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient carbon report summary is in its life cycle. */
enum class LenientCarbonReportSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a lenient carbon report summary and its current status. */
data class LenientCarbonReportSummary(
    var code: String,
    var name: String,
    var trailerId: String,
    var retries: Int,
    var ratio: Double,
    var region: String,
    var dock: String,
    var updatedAt: Int,
    var timezone: String,
    var status: LenientCarbonReportSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCarbonReportSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCarbonReportSummaryStatus): Boolean {
        when (status) {
            LenientCarbonReportSummaryStatus.PLANNED -> {
                when (next) {
                    LenientCarbonReportSummaryStatus.SCHEDULED, LenientCarbonReportSummaryStatus.IN_PROGRESS -> return true
                }
                return false
            }
            LenientCarbonReportSummaryStatus.SCHEDULED -> {
                when (next) {
                    LenientCarbonReportSummaryStatus.IN_PROGRESS, LenientCarbonReportSummaryStatus.CANCELLED -> return true
                }
                return false
            }
            LenientCarbonReportSummaryStatus.IN_PROGRESS -> return next == LenientCarbonReportSummaryStatus.COMPLETED || next == LenientCarbonReportSummaryStatus.CANCELLED
            LenientCarbonReportSummaryStatus.COMPLETED -> {
                when (next) {
                    LenientCarbonReportSummaryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientCarbonReportSummaryStatus) {
        if (!canMoveTo(next)) {
            error("lenient carbon report summary $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun projectedTotal(): Double = round(retries.toDouble() * ratio * 100) / 100

    fun snapshot(): LenientCarbonReportSummary {
        return LenientCarbonReportSummary(
            code = code,
            name = name,
            trailerId = trailerId,
            retries = retries,
            ratio = ratio,
            region = region,
            dock = dock,
            updatedAt = updatedAt,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a lenient carbon report summary in status planned. */
fun newLenientCarbonReportSummary(code: String, name: String): LenientCarbonReportSummary {
    return LenientCarbonReportSummary(
        code = code,
        name = name,
        trailerId = "",
        retries = 0,
        ratio = 0.0,
        region = "",
        dock = "",
        updatedAt = 0,
        timezone = "",
        status = LenientCarbonReportSummaryStatus.PLANNED,
    )
}
