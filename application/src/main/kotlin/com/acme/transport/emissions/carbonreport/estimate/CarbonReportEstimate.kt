package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The carbon report estimate records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a carbon report estimate is in its life cycle. */
enum class CarbonReportEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A carbon report estimate as the carbon report team keeps it. */
data class CarbonReportEstimate(
    var code: String,
    var title: String,
    var description: String,
    var graceDays: Int,
    var reviewer: String,
    var carrier: String,
    var capacity: Int,
    var tags: MutableList<String>,
    var depot: String,
    var active: Boolean,
    var status: CarbonReportEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportEstimateStatus): Boolean {
        when (status) {
            CarbonReportEstimateStatus.CREATED -> return next == CarbonReportEstimateStatus.VALIDATED || next == CarbonReportEstimateStatus.RETIRED
            CarbonReportEstimateStatus.VALIDATED -> {
                when (next) {
                    CarbonReportEstimateStatus.PUBLISHED -> return true
                }
                return false
            }
            CarbonReportEstimateStatus.PUBLISHED -> return next == CarbonReportEstimateStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: CarbonReportEstimateStatus) {
        if (!canMoveTo(next)) {
            error("carbon report estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"carbon report estimate"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun adjustedExposure(): Double = round((graceDays.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): CarbonReportEstimate {
        return CarbonReportEstimate(
            code = code,
            title = title,
            description = description,
            graceDays = graceDays,
            reviewer = reviewer,
            carrier = carrier,
            capacity = capacity,
            tags = tags,
            depot = depot,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a carbon report estimate in status created. */
fun newCarbonReportEstimate(code: String, title: String): CarbonReportEstimate {
    return CarbonReportEstimate(
        code = code,
        title = title,
        description = "",
        graceDays = 0,
        reviewer = "",
        carrier = "",
        capacity = 0,
        tags = mutableListOf<String>(),
        depot = "",
        active = false,
        status = CarbonReportEstimateStatus.CREATED,
    )
}
