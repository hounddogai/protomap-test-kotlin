package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report summary is in its life cycle. */
enum class CarbonReportSummaryStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a carbon report summary and its current status. */
data class CarbonReportSummary(
    var code: String,
    var title: String,
    var channel: String,
    var margin: Double,
    var pallets: Int,
    var visible: Boolean,
    var carrier: String,
    var status: CarbonReportSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportSummaryStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportSummaryStatus): Boolean {
        when (status) {
            CarbonReportSummaryStatus.PLANNED -> {
                when (next) {
                    CarbonReportSummaryStatus.SCHEDULED, CarbonReportSummaryStatus.CANCELLED -> return true
                }
                return false
            }
            CarbonReportSummaryStatus.SCHEDULED -> return next == CarbonReportSummaryStatus.COMPLETED
            CarbonReportSummaryStatus.IN_PROGRESS -> {
                when (next) {
                    CarbonReportSummaryStatus.COMPLETED -> return true
                }
                return false
            }
            CarbonReportSummaryStatus.COMPLETED -> {
                when (next) {
                    CarbonReportSummaryStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CarbonReportSummaryStatus) {
        if (!canMoveTo(next)) {
            error("carbon report summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netExposure(): Double = round((margin + pallets.toDouble()) * 100) / 100

    fun snapshot(): CarbonReportSummary {
        return CarbonReportSummary(
            code = code,
            title = title,
            channel = channel,
            margin = margin,
            pallets = pallets,
            visible = visible,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a carbon report summary in status planned. */
fun newCarbonReportSummary(code: String, title: String): CarbonReportSummary {
    return CarbonReportSummary(
        code = code,
        title = title,
        channel = "",
        margin = 0.0,
        pallets = 0,
        visible = false,
        carrier = "",
        status = CarbonReportSummaryStatus.PLANNED,
    )
}
