package com.acme.transport.emissions.carbonreport.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The carbon report claim records of emissions's carbon report work, and the rules, stores, and calculations
// around them.

/** Where a carbon report claim is in its life cycle. */
enum class CarbonReportClaimStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A carbon report claim. Its code is unique within a tenant. */
data class CarbonReportClaim(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var approved: Boolean,
    var cost: Double,
    var graceDays: Int,
    var stopCount: Int,
    var status: CarbonReportClaimStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportClaimStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportClaimStatus): Boolean {
        when (status) {
            CarbonReportClaimStatus.OPEN -> {
                when (next) {
                    CarbonReportClaimStatus.IN_REVIEW -> return true
                }
                return false
            }
            CarbonReportClaimStatus.IN_REVIEW -> {
                when (next) {
                    CarbonReportClaimStatus.REOPENED, CarbonReportClaimStatus.ARCHIVED -> return true
                }
                return false
            }
            CarbonReportClaimStatus.RESOLVED -> return next == CarbonReportClaimStatus.ARCHIVED
            CarbonReportClaimStatus.REOPENED -> {
                when (next) {
                    CarbonReportClaimStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: CarbonReportClaimStatus) {
        if (!canMoveTo(next)) {
            error("carbon report claim $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"carbon report claim"} $code"

    fun weightedValue(): Double = round(stopCount.toDouble() * cost * 100) / 100

    fun snapshot(): CarbonReportClaim {
        return CarbonReportClaim(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            approved = approved,
            cost = cost,
            graceDays = graceDays,
            stopCount = stopCount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a carbon report claim in status open. */
fun newCarbonReportClaim(code: String, label: String): CarbonReportClaim {
    return CarbonReportClaim(
        code = code,
        label = label,
        durationMinutes = 0,
        approved = false,
        cost = 0.0,
        graceDays = 0,
        stopCount = 0,
        status = CarbonReportClaimStatus.OPEN,
    )
}
