package com.acme.transport.emissions.carbonreport.rule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report milestone is in its life cycle. */
enum class CarbonReportMilestoneStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One carbon report milestone, with what the planners need to act on it. */
data class CarbonReportMilestone(
    var code: String,
    var label: String,
    var trailerId: String,
    var depot: String,
    var discount: Double,
    var graceDays: Int,
    var floorValue: Double,
    var version: Int,
    var stopCount: Int,
    var priority: Int,
    var status: CarbonReportMilestoneStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportMilestoneStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportMilestoneStatus): Boolean {
        when (status) {
            CarbonReportMilestoneStatus.CREATED -> return next == CarbonReportMilestoneStatus.VALIDATED
            CarbonReportMilestoneStatus.VALIDATED -> return next == CarbonReportMilestoneStatus.PUBLISHED || next == CarbonReportMilestoneStatus.RETIRED
            CarbonReportMilestoneStatus.PUBLISHED -> {
                when (next) {
                    CarbonReportMilestoneStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CarbonReportMilestoneStatus) {
        if (!canMoveTo(next)) {
            error("carbon report milestone $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"carbon report milestone"} $code"

    fun projectedTotal(): Double = round((stopCount.toDouble() + discount) * 100) / 100

    fun snapshot(): CarbonReportMilestone {
        return CarbonReportMilestone(
            code = code,
            label = label,
            trailerId = trailerId,
            depot = depot,
            discount = discount,
            graceDays = graceDays,
            floorValue = floorValue,
            version = version,
            stopCount = stopCount,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a carbon report milestone in status created. */
fun newCarbonReportMilestone(code: String, label: String): CarbonReportMilestone {
    return CarbonReportMilestone(
        code = code,
        label = label,
        trailerId = "",
        depot = "",
        discount = 0.0,
        graceDays = 0,
        floorValue = 0.0,
        version = 0,
        stopCount = 0,
        priority = 0,
        status = CarbonReportMilestoneStatus.CREATED,
    )
}
