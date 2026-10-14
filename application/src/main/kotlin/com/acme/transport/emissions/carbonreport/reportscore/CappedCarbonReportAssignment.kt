package com.acme.transport.emissions.carbonreport.reportscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a capped carbon report assignment is in its life cycle. */
enum class CappedCarbonReportAssignmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A capped carbon report assignment as the carbon report team keeps it. */
data class CappedCarbonReportAssignment(
    var code: String,
    var label: String,
    var description: String,
    var etaMinutes: Int,
    var externalRef: String,
    var contactEmail: String,
    var sequence: Int,
    var category: String,
    var status: CappedCarbonReportAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CappedCarbonReportAssignmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CappedCarbonReportAssignmentStatus): Boolean {
        when (status) {
            CappedCarbonReportAssignmentStatus.OPEN -> return next == CappedCarbonReportAssignmentStatus.RESOLVED
            CappedCarbonReportAssignmentStatus.IN_REVIEW -> return next == CappedCarbonReportAssignmentStatus.RESOLVED || next == CappedCarbonReportAssignmentStatus.REOPENED
            CappedCarbonReportAssignmentStatus.RESOLVED -> {
                when (next) {
                    CappedCarbonReportAssignmentStatus.REOPENED -> return true
                }
                return false
            }
            CappedCarbonReportAssignmentStatus.REOPENED -> {
                when (next) {
                    CappedCarbonReportAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CappedCarbonReportAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("capped carbon report assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun netExposure(): Double = round(sequence.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): CappedCarbonReportAssignment {
        return CappedCarbonReportAssignment(
            code = code,
            label = label,
            description = description,
            etaMinutes = etaMinutes,
            externalRef = externalRef,
            contactEmail = contactEmail,
            sequence = sequence,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a capped carbon report assignment in status open. */
fun newCappedCarbonReportAssignment(code: String, label: String): CappedCarbonReportAssignment {
    return CappedCarbonReportAssignment(
        code = code,
        label = label,
        description = "",
        etaMinutes = 0,
        externalRef = "",
        contactEmail = "",
        sequence = 0,
        category = "",
        status = CappedCarbonReportAssignmentStatus.OPEN,
    )
}
