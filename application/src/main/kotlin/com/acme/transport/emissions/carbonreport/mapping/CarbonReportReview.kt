package com.acme.transport.emissions.carbonreport.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report review is in its life cycle. */
enum class CarbonReportReviewStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One carbon report review, with what the planners need to act on it. */
data class CarbonReportReview(
    var code: String,
    var label: String,
    var minimum: Double,
    var channel: String,
    var summary: String,
    var depot: String,
    var contactEmail: String,
    var loadKg: Double,
    var region: String,
    var dock: String,
    var lane: String,
    var status: CarbonReportReviewStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportReviewStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportReviewStatus): Boolean {
        when (status) {
            CarbonReportReviewStatus.IDLE -> {
                when (next) {
                    CarbonReportReviewStatus.WARMING, CarbonReportReviewStatus.STOPPED -> return true
                }
                return false
            }
            CarbonReportReviewStatus.WARMING -> {
                when (next) {
                    CarbonReportReviewStatus.READY, CarbonReportReviewStatus.DRAINING -> return true
                }
                return false
            }
            CarbonReportReviewStatus.READY -> return next == CarbonReportReviewStatus.DRAINING || next == CarbonReportReviewStatus.STOPPED
            CarbonReportReviewStatus.DRAINING -> return next == CarbonReportReviewStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CarbonReportReviewStatus) {
        if (!canMoveTo(next)) {
            error("carbon report review $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedLoad(): Double = round((loadKg + minimum) * 100) / 100

    fun snapshot(): CarbonReportReview {
        return CarbonReportReview(
            code = code,
            label = label,
            minimum = minimum,
            channel = channel,
            summary = summary,
            depot = depot,
            contactEmail = contactEmail,
            loadKg = loadKg,
            region = region,
            dock = dock,
            lane = lane,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a carbon report review in status idle. */
fun newCarbonReportReview(code: String, label: String): CarbonReportReview {
    return CarbonReportReview(
        code = code,
        label = label,
        minimum = 0.0,
        channel = "",
        summary = "",
        depot = "",
        contactEmail = "",
        loadKg = 0.0,
        region = "",
        dock = "",
        lane = "",
        status = CarbonReportReviewStatus.IDLE,
    )
}
