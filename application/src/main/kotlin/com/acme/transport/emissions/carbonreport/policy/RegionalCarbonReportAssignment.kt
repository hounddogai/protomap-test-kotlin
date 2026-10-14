package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional carbon report assignment is in its life cycle. */
enum class RegionalCarbonReportAssignmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A regional carbon report assignment as the carbon report team keeps it. */
data class RegionalCarbonReportAssignment(
    var code: String,
    var name: String,
    var minimum: Double,
    var budget: Double,
    var windowDays: Int,
    var sequence: Int,
    var status: RegionalCarbonReportAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCarbonReportAssignmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCarbonReportAssignmentStatus): Boolean {
        when (status) {
            RegionalCarbonReportAssignmentStatus.PROPOSED -> return next == RegionalCarbonReportAssignmentStatus.WITHDRAWN
            RegionalCarbonReportAssignmentStatus.APPROVED -> {
                when (next) {
                    RegionalCarbonReportAssignmentStatus.REJECTED, RegionalCarbonReportAssignmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            RegionalCarbonReportAssignmentStatus.REJECTED -> {
                when (next) {
                    RegionalCarbonReportAssignmentStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCarbonReportAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("regional carbon report assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedVolume(): Double = round((budget + minimum) * 100) / 100

    fun snapshot(): RegionalCarbonReportAssignment {
        return RegionalCarbonReportAssignment(
            code = code,
            name = name,
            minimum = minimum,
            budget = budget,
            windowDays = windowDays,
            sequence = sequence,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a regional carbon report assignment in status proposed. */
fun newRegionalCarbonReportAssignment(code: String, name: String): RegionalCarbonReportAssignment {
    return RegionalCarbonReportAssignment(
        code = code,
        name = name,
        minimum = 0.0,
        budget = 0.0,
        windowDays = 0,
        sequence = 0,
        status = RegionalCarbonReportAssignmentStatus.PROPOSED,
    )
}
