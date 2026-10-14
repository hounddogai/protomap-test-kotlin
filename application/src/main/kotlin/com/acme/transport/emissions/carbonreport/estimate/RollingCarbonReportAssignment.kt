package com.acme.transport.emissions.carbonreport.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling carbon report assignment is in its life cycle. */
enum class RollingCarbonReportAssignmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rolling carbon report assignment as the carbon report team keeps it. */
data class RollingCarbonReportAssignment(
    var code: String,
    var name: String,
    var endsAt: Int,
    var capacity: Int,
    var threshold: Double,
    var depot: String,
    var lane: String,
    var externalRef: String,
    var rate: Double,
    var status: RollingCarbonReportAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCarbonReportAssignmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCarbonReportAssignmentStatus): Boolean {
        when (status) {
            RollingCarbonReportAssignmentStatus.PROPOSED -> {
                when (next) {
                    RollingCarbonReportAssignmentStatus.APPROVED, RollingCarbonReportAssignmentStatus.REJECTED -> return true
                }
                return false
            }
            RollingCarbonReportAssignmentStatus.APPROVED -> {
                when (next) {
                    RollingCarbonReportAssignmentStatus.REJECTED, RollingCarbonReportAssignmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingCarbonReportAssignmentStatus.REJECTED -> return next == RollingCarbonReportAssignmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RollingCarbonReportAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("rolling carbon report assignment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun projectedExposure(): Double = round((rate + endsAt.toDouble()) * 100) / 100

    fun snapshot(): RollingCarbonReportAssignment {
        return RollingCarbonReportAssignment(
            code = code,
            name = name,
            endsAt = endsAt,
            capacity = capacity,
            threshold = threshold,
            depot = depot,
            lane = lane,
            externalRef = externalRef,
            rate = rate,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a rolling carbon report assignment in status proposed. */
fun newRollingCarbonReportAssignment(code: String, name: String): RollingCarbonReportAssignment {
    return RollingCarbonReportAssignment(
        code = code,
        name = name,
        endsAt = 0,
        capacity = 0,
        threshold = 0.0,
        depot = "",
        lane = "",
        externalRef = "",
        rate = 0.0,
        status = RollingCarbonReportAssignmentStatus.PROPOSED,
    )
}
