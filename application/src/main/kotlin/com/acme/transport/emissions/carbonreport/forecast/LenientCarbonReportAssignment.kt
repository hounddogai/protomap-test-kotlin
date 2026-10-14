package com.acme.transport.emissions.carbonreport.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient carbon report assignment is in its life cycle. */
enum class LenientCarbonReportAssignmentStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A lenient carbon report assignment. Its code is unique within a tenant. */
data class LenientCarbonReportAssignment(
    var code: String,
    var title: String,
    var discount: Double,
    var externalRef: String,
    var durationMinutes: Int,
    var contactEmail: String,
    var status: LenientCarbonReportAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientCarbonReportAssignmentStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientCarbonReportAssignmentStatus): Boolean {
        when (status) {
            LenientCarbonReportAssignmentStatus.OPEN -> {
                when (next) {
                    LenientCarbonReportAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientCarbonReportAssignmentStatus.IN_REVIEW -> {
                when (next) {
                    LenientCarbonReportAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
            LenientCarbonReportAssignmentStatus.RESOLVED -> {
                when (next) {
                    LenientCarbonReportAssignmentStatus.REOPENED -> return true
                }
                return false
            }
            LenientCarbonReportAssignmentStatus.REOPENED -> {
                when (next) {
                    LenientCarbonReportAssignmentStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: LenientCarbonReportAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("lenient carbon report assignment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient carbon report assignment"} $code"

    fun effectiveTotal(): Double = round(durationMinutes.toDouble() * discount * 100) / 100

    fun snapshot(): LenientCarbonReportAssignment {
        return LenientCarbonReportAssignment(
            code = code,
            title = title,
            discount = discount,
            externalRef = externalRef,
            durationMinutes = durationMinutes,
            contactEmail = contactEmail,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a lenient carbon report assignment in status open. */
fun newLenientCarbonReportAssignment(code: String, title: String): LenientCarbonReportAssignment {
    return LenientCarbonReportAssignment(
        code = code,
        title = title,
        discount = 0.0,
        externalRef = "",
        durationMinutes = 0,
        contactEmail = "",
        status = LenientCarbonReportAssignmentStatus.OPEN,
    )
}
