package com.acme.transport.emissions.carbonreport.reportestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling carbon report override is in its life cycle. */
enum class RollingCarbonReportOverrideStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A rolling carbon report override. Its code is unique within a tenant. */
data class RollingCarbonReportOverride(
    var code: String,
    var label: String,
    var sequence: Int,
    var etaMinutes: Int,
    var windowDays: Int,
    var category: String,
    var timezone: String,
    var margin: Double,
    var status: RollingCarbonReportOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingCarbonReportOverrideStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingCarbonReportOverrideStatus): Boolean {
        when (status) {
            RollingCarbonReportOverrideStatus.PROPOSED -> {
                when (next) {
                    RollingCarbonReportOverrideStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingCarbonReportOverrideStatus.APPROVED -> return next == RollingCarbonReportOverrideStatus.REJECTED || next == RollingCarbonReportOverrideStatus.WITHDRAWN
            RollingCarbonReportOverrideStatus.REJECTED -> return next == RollingCarbonReportOverrideStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RollingCarbonReportOverrideStatus) {
        if (!canMoveTo(next)) {
            error("rolling carbon report override $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"rolling carbon report override"} $code"

    fun projectedVolume(): Double = round((etaMinutes.toDouble() + windowDays.toDouble()) * 100) / 100

    fun snapshot(): RollingCarbonReportOverride {
        return RollingCarbonReportOverride(
            code = code,
            label = label,
            sequence = sequence,
            etaMinutes = etaMinutes,
            windowDays = windowDays,
            category = category,
            timezone = timezone,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a rolling carbon report override in status proposed. */
fun newRollingCarbonReportOverride(code: String, label: String): RollingCarbonReportOverride {
    return RollingCarbonReportOverride(
        code = code,
        label = label,
        sequence = 0,
        etaMinutes = 0,
        windowDays = 0,
        category = "",
        timezone = "",
        margin = 0.0,
        status = RollingCarbonReportOverrideStatus.PROPOSED,
    )
}
