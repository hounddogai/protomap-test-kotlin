package com.acme.transport.emissions.carbonreport.digest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report assignment is in its life cycle. */
enum class CarbonReportAssignmentStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A carbon report assignment. Its code is unique within a tenant. */
data class CarbonReportAssignment(
    var code: String,
    var name: String,
    var trailerId: String,
    var expiresAt: Int,
    var cost: Double,
    var active: Boolean,
    var currency: String,
    var windowDays: Int,
    var status: CarbonReportAssignmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportAssignmentStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportAssignmentStatus): Boolean {
        when (status) {
            CarbonReportAssignmentStatus.REQUESTED -> {
                when (next) {
                    CarbonReportAssignmentStatus.REVOKED, CarbonReportAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            CarbonReportAssignmentStatus.GRANTED -> {
                when (next) {
                    CarbonReportAssignmentStatus.REVOKED, CarbonReportAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
            CarbonReportAssignmentStatus.REVOKED -> {
                when (next) {
                    CarbonReportAssignmentStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CarbonReportAssignmentStatus) {
        if (!canMoveTo(next)) {
            error("carbon report assignment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"carbon report assignment"} $code"

    fun effectiveLoad(): Double = round((cost - expiresAt.toDouble()) * 100) / 100

    fun snapshot(): CarbonReportAssignment {
        return CarbonReportAssignment(
            code = code,
            name = name,
            trailerId = trailerId,
            expiresAt = expiresAt,
            cost = cost,
            active = active,
            currency = currency,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a carbon report assignment in status requested. */
fun newCarbonReportAssignment(code: String, name: String): CarbonReportAssignment {
    return CarbonReportAssignment(
        code = code,
        name = name,
        trailerId = "",
        expiresAt = 0,
        cost = 0.0,
        active = false,
        currency = "",
        windowDays = 0,
        status = CarbonReportAssignmentStatus.REQUESTED,
    )
}
