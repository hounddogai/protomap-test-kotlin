package com.acme.transport.emissions.carbonreport.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The carbon report adjustment records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a carbon report adjustment is in its life cycle. */
enum class CarbonReportAdjustmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One carbon report adjustment, with what the planners need to act on it. */
data class CarbonReportAdjustment(
    var code: String,
    var name: String,
    var maximum: Double,
    var trailerId: String,
    var weight: Double,
    var shiftCode: String,
    var phone: String,
    var updatedAt: Int,
    var status: CarbonReportAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportAdjustmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportAdjustmentStatus): Boolean {
        when (status) {
            CarbonReportAdjustmentStatus.PROPOSED -> return next == CarbonReportAdjustmentStatus.APPROVED
            CarbonReportAdjustmentStatus.APPROVED -> {
                when (next) {
                    CarbonReportAdjustmentStatus.REJECTED, CarbonReportAdjustmentStatus.WITHDRAWN -> return true
                }
                return false
            }
            CarbonReportAdjustmentStatus.REJECTED -> return next == CarbonReportAdjustmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CarbonReportAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("carbon report adjustment $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"carbon report adjustment"} $code"

    fun weightedValue(): Double = round(maximum * weight * 100) / 100

    fun snapshot(): CarbonReportAdjustment {
        return CarbonReportAdjustment(
            code = code,
            name = name,
            maximum = maximum,
            trailerId = trailerId,
            weight = weight,
            shiftCode = shiftCode,
            phone = phone,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a carbon report adjustment in status proposed. */
fun newCarbonReportAdjustment(code: String, name: String): CarbonReportAdjustment {
    return CarbonReportAdjustment(
        code = code,
        name = name,
        maximum = 0.0,
        trailerId = "",
        weight = 0.0,
        shiftCode = "",
        phone = "",
        updatedAt = 0,
        status = CarbonReportAdjustmentStatus.PROPOSED,
    )
}
