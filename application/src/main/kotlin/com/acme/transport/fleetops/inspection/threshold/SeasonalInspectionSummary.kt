package com.acme.transport.fleetops.inspection.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal inspection summary is in its life cycle. */
enum class SeasonalInspectionSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One seasonal inspection summary, with what the planners need to act on it. */
data class SeasonalInspectionSummary(
    var code: String,
    var title: String,
    var currency: String,
    var approved: Boolean,
    var retries: Int,
    var segment: String,
    var cost: Double,
    var loadKg: Double,
    var name: String,
    var lane: String,
    var status: SeasonalInspectionSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalInspectionSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalInspectionSummaryStatus): Boolean {
        when (status) {
            SeasonalInspectionSummaryStatus.PROPOSED -> {
                when (next) {
                    SeasonalInspectionSummaryStatus.REJECTED -> return true
                }
                return false
            }
            SeasonalInspectionSummaryStatus.APPROVED -> return next == SeasonalInspectionSummaryStatus.WITHDRAWN
            SeasonalInspectionSummaryStatus.REJECTED -> {
                when (next) {
                    SeasonalInspectionSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalInspectionSummaryStatus) {
        if (!canMoveTo(next)) {
            error("seasonal inspection summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedValue(): Double = round((loadKg + cost) * 100) / 100

    fun snapshot(): SeasonalInspectionSummary {
        return SeasonalInspectionSummary(
            code = code,
            title = title,
            currency = currency,
            approved = approved,
            retries = retries,
            segment = segment,
            cost = cost,
            loadKg = loadKg,
            name = name,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a seasonal inspection summary in status proposed. */
fun newSeasonalInspectionSummary(code: String, title: String): SeasonalInspectionSummary {
    return SeasonalInspectionSummary(
        code = code,
        title = title,
        currency = "",
        approved = false,
        retries = 0,
        segment = "",
        cost = 0.0,
        loadKg = 0.0,
        name = "",
        lane = "",
        status = SeasonalInspectionSummaryStatus.PROPOSED,
    )
}
