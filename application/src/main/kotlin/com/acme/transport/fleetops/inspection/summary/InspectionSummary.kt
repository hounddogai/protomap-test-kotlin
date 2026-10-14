package com.acme.transport.fleetops.inspection.summary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The inspection summary records of fleetops's inspection work, and the rules, stores, and calculations
// around them.

/** Where a inspection summary is in its life cycle. */
enum class InspectionSummaryStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a inspection summary and its current status. */
data class InspectionSummary(
    var code: String,
    var title: String,
    var priority: Int,
    var channel: String,
    var threshold: Double,
    var budget: Double,
    var shiftCode: String,
    var status: InspectionSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            InspectionSummaryStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: InspectionSummaryStatus): Boolean {
        when (status) {
            InspectionSummaryStatus.OPEN -> {
                when (next) {
                    InspectionSummaryStatus.RESOLVED -> return true
                }
                return false
            }
            InspectionSummaryStatus.IN_REVIEW -> return next == InspectionSummaryStatus.RESOLVED
            InspectionSummaryStatus.RESOLVED -> {
                when (next) {
                    InspectionSummaryStatus.REOPENED, InspectionSummaryStatus.ARCHIVED -> return true
                }
                return false
            }
            InspectionSummaryStatus.REOPENED -> {
                when (next) {
                    InspectionSummaryStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: InspectionSummaryStatus) {
        if (!canMoveTo(next)) {
            error("inspection summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveLoad(): Double = round((budget - threshold) * 100) / 100

    fun snapshot(): InspectionSummary {
        return InspectionSummary(
            code = code,
            title = title,
            priority = priority,
            channel = channel,
            threshold = threshold,
            budget = budget,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a inspection summary in status open. */
fun newInspectionSummary(code: String, title: String): InspectionSummary {
    return InspectionSummary(
        code = code,
        title = title,
        priority = 0,
        channel = "",
        threshold = 0.0,
        budget = 0.0,
        shiftCode = "",
        status = InspectionSummaryStatus.OPEN,
    )
}
