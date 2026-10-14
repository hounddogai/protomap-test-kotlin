package com.acme.transport.emissions.carbonreport.reportmapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global carbon report adjustment is in its life cycle. */
enum class GlobalCarbonReportAdjustmentStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A global carbon report adjustment. Its code is unique within a tenant. */
data class GlobalCarbonReportAdjustment(
    var code: String,
    var name: String,
    var expiresAt: Int,
    var visible: Boolean,
    var approved: Boolean,
    var minimum: Double,
    var summary: String,
    var budget: Double,
    var status: GlobalCarbonReportAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalCarbonReportAdjustmentStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalCarbonReportAdjustmentStatus): Boolean {
        when (status) {
            GlobalCarbonReportAdjustmentStatus.CREATED -> return next == GlobalCarbonReportAdjustmentStatus.VALIDATED || next == GlobalCarbonReportAdjustmentStatus.RETIRED
            GlobalCarbonReportAdjustmentStatus.VALIDATED -> return next == GlobalCarbonReportAdjustmentStatus.PUBLISHED
            GlobalCarbonReportAdjustmentStatus.PUBLISHED -> {
                when (next) {
                    GlobalCarbonReportAdjustmentStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalCarbonReportAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("global carbon report adjustment $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code: $name"

    fun adjustedExposure(): Double = round(minimum * budget * 100) / 100

    fun snapshot(): GlobalCarbonReportAdjustment {
        return GlobalCarbonReportAdjustment(
            code = code,
            name = name,
            expiresAt = expiresAt,
            visible = visible,
            approved = approved,
            minimum = minimum,
            summary = summary,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global carbon report adjustment in status created. */
fun newGlobalCarbonReportAdjustment(code: String, name: String): GlobalCarbonReportAdjustment {
    return GlobalCarbonReportAdjustment(
        code = code,
        name = name,
        expiresAt = 0,
        visible = false,
        approved = false,
        minimum = 0.0,
        summary = "",
        budget = 0.0,
        status = GlobalCarbonReportAdjustmentStatus.CREATED,
    )
}
