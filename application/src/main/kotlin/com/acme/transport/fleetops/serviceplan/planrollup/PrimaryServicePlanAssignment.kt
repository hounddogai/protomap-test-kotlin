package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.kit.text.normalize

/** Where a primary service plan assignment is in its life cycle. */
enum class PrimaryServicePlanAssignmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One primary service plan assignment, with what the planners need to act on it. */
data class PrimaryServicePlanAssignment(
    var code: String,
    var name: String,
    var country: String,
    var driverId: String,
    var maximum: Double,
    var shiftCode: String,
    var checksum: String,
    var locale: String,
    var status: PrimaryServicePlanAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryServicePlanAssignmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryServicePlanAssignmentStatus): Boolean {
        when (status) {
            PrimaryServicePlanAssignmentStatus.OPEN -> {
                when (next) {
                    PrimaryServicePlanAssignmentStatus.REOPENED -> return true
                }
                return false
            }
            PrimaryServicePlanAssignmentStatus.IN_REVIEW -> return next == PrimaryServicePlanAssignmentStatus.ARCHIVED
            PrimaryServicePlanAssignmentStatus.RESOLVED -> return next == PrimaryServicePlanAssignmentStatus.ARCHIVED
            PrimaryServicePlanAssignmentStatus.REOPENED -> {
                when (next) {
                    PrimaryServicePlanAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryServicePlanAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("primary service plan assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun measuredValue(): Double = maximum

    fun snapshot(): PrimaryServicePlanAssignment {
        return PrimaryServicePlanAssignment(
            code = code,
            name = name,
            country = country,
            driverId = driverId,
            maximum = maximum,
            shiftCode = shiftCode,
            checksum = checksum,
            locale = locale,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a primary service plan assignment in status open. */
fun newPrimaryServicePlanAssignment(code: String, name: String): PrimaryServicePlanAssignment {
    return PrimaryServicePlanAssignment(
        code = code,
        name = name,
        country = "",
        driverId = "",
        maximum = 0.0,
        shiftCode = "",
        checksum = "",
        locale = "",
        status = PrimaryServicePlanAssignmentStatus.OPEN,
    )
}
