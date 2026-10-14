package com.acme.transport.emissions.carbonreport.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive carbon report group is in its life cycle. */
enum class AdaptiveCarbonReportGroupStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A adaptive carbon report group. Its code is unique within a tenant. */
data class AdaptiveCarbonReportGroup(
    var code: String,
    var title: String,
    var fuelLiters: Double,
    var country: String,
    var retries: Int,
    var carrier: String,
    var segment: String,
    var status: AdaptiveCarbonReportGroupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveCarbonReportGroupStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveCarbonReportGroupStatus): Boolean {
        when (status) {
            AdaptiveCarbonReportGroupStatus.OPEN -> return next == AdaptiveCarbonReportGroupStatus.RESOLVED || next == AdaptiveCarbonReportGroupStatus.ARCHIVED
            AdaptiveCarbonReportGroupStatus.IN_REVIEW -> return next == AdaptiveCarbonReportGroupStatus.ARCHIVED
            AdaptiveCarbonReportGroupStatus.RESOLVED -> return next == AdaptiveCarbonReportGroupStatus.REOPENED
            AdaptiveCarbonReportGroupStatus.REOPENED -> {
                when (next) {
                    AdaptiveCarbonReportGroupStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveCarbonReportGroupStatus) {
        if (!canMoveTo(next)) {
            error("adaptive carbon report group $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"adaptive carbon report group"} $code"

    fun netValue(): Double = round((retries.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): AdaptiveCarbonReportGroup {
        return AdaptiveCarbonReportGroup(
            code = code,
            title = title,
            fuelLiters = fuelLiters,
            country = country,
            retries = retries,
            carrier = carrier,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a adaptive carbon report group in status open. */
fun newAdaptiveCarbonReportGroup(code: String, title: String): AdaptiveCarbonReportGroup {
    return AdaptiveCarbonReportGroup(
        code = code,
        title = title,
        fuelLiters = 0.0,
        country = "",
        retries = 0,
        carrier = "",
        segment = "",
        status = AdaptiveCarbonReportGroupStatus.OPEN,
    )
}
