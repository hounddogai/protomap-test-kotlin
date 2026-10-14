package com.acme.transport.crewing.dutyperiod.manualperiodassignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The capped duty period assignment records of crewing's duty period work, and the rules, stores, and
// calculations around them.

/** Where a capped duty period assignment is in its life cycle. */
enum class CappedDutyPeriodAssignmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A capped duty period assignment. Its code is unique within a tenant. */
data class CappedDutyPeriodAssignment(
    var code: String,
    var title: String,
    var limit: Int,
    var discount: Double,
    var driverId: String,
    var retries: Int,
    var reviewer: String,
    var trailerId: String,
    var status: CappedDutyPeriodAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedDutyPeriodAssignmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedDutyPeriodAssignmentStatus): Boolean {
        when (status) {
            CappedDutyPeriodAssignmentStatus.PLANNED -> return next == CappedDutyPeriodAssignmentStatus.SCHEDULED || next == CappedDutyPeriodAssignmentStatus.COMPLETED
            CappedDutyPeriodAssignmentStatus.SCHEDULED -> return next == CappedDutyPeriodAssignmentStatus.CANCELLED
            CappedDutyPeriodAssignmentStatus.IN_PROGRESS -> {
                when (next) {
                    CappedDutyPeriodAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
            CappedDutyPeriodAssignmentStatus.COMPLETED -> {
                when (next) {
                    CappedDutyPeriodAssignmentStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedDutyPeriodAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("capped duty period assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun effectiveVolume(): Double = round((discount + retries.toDouble()) * 100) / 100

    fun snapshot(): CappedDutyPeriodAssignment {
        return CappedDutyPeriodAssignment(
            code = code,
            title = title,
            limit = limit,
            discount = discount,
            driverId = driverId,
            retries = retries,
            reviewer = reviewer,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capped duty period assignment in status planned. */
fun newCappedDutyPeriodAssignment(code: String, title: String): CappedDutyPeriodAssignment {
    return CappedDutyPeriodAssignment(
        code = code,
        title = title,
        limit = 0,
        discount = 0.0,
        driverId = "",
        retries = 0,
        reviewer = "",
        trailerId = "",
        status = CappedDutyPeriodAssignmentStatus.PLANNED,
    )
}
