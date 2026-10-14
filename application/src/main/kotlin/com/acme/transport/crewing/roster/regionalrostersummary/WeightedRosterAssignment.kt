package com.acme.transport.crewing.roster.regionalrostersummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted roster assignment is in its life cycle. */
enum class WeightedRosterAssignmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One weighted roster assignment, with what the planners need to act on it. */
data class WeightedRosterAssignment(
    var code: String,
    var title: String,
    var capacity: Int,
    var pinned: Boolean,
    var depot: String,
    var maximum: Double,
    var retries: Int,
    var status: WeightedRosterAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRosterAssignmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRosterAssignmentStatus): Boolean {
        when (status) {
            WeightedRosterAssignmentStatus.CREATED -> return next == WeightedRosterAssignmentStatus.RETIRED
            WeightedRosterAssignmentStatus.VALIDATED -> return next == WeightedRosterAssignmentStatus.RETIRED
            WeightedRosterAssignmentStatus.PUBLISHED -> return next == WeightedRosterAssignmentStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: WeightedRosterAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("weighted roster assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted roster assignment"} $code"

    fun projectedExposure(): Double = round((retries.toDouble() + maximum) * 100) / 100

    fun snapshot(): WeightedRosterAssignment {
        return WeightedRosterAssignment(
            code = code,
            title = title,
            capacity = capacity,
            pinned = pinned,
            depot = depot,
            maximum = maximum,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a weighted roster assignment in status created. */
fun newWeightedRosterAssignment(code: String, title: String): WeightedRosterAssignment {
    return WeightedRosterAssignment(
        code = code,
        title = title,
        capacity = 0,
        pinned = false,
        depot = "",
        maximum = 0.0,
        retries = 0,
        status = WeightedRosterAssignmentStatus.CREATED,
    )
}
