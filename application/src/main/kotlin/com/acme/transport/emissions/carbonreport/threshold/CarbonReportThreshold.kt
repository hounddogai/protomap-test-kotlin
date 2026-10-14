package com.acme.transport.emissions.carbonreport.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The carbon report threshold records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a carbon report threshold is in its life cycle. */
enum class CarbonReportThresholdStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A carbon report threshold as the carbon report team keeps it. */
data class CarbonReportThreshold(
    var code: String,
    var title: String,
    var minimum: Double,
    var windowDays: Int,
    var region: String,
    var status: CarbonReportThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportThresholdStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportThresholdStatus): Boolean {
        when (status) {
            CarbonReportThresholdStatus.IDLE -> return next == CarbonReportThresholdStatus.STOPPED
            CarbonReportThresholdStatus.WARMING -> {
                when (next) {
                    CarbonReportThresholdStatus.STOPPED -> return true
                }
                return false
            }
            CarbonReportThresholdStatus.READY -> {
                when (next) {
                    CarbonReportThresholdStatus.DRAINING, CarbonReportThresholdStatus.STOPPED -> return true
                }
                return false
            }
            CarbonReportThresholdStatus.DRAINING -> return next == CarbonReportThresholdStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: CarbonReportThresholdStatus) {
        if (!canMoveTo(next)) {
            error("carbon report threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun projectedVolume(): Double = round(minimum * windowDays.toDouble() * 100) / 100

    fun snapshot(): CarbonReportThreshold {
        return CarbonReportThreshold(
            code = code,
            title = title,
            minimum = minimum,
            windowDays = windowDays,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a carbon report threshold in status idle. */
fun newCarbonReportThreshold(code: String, title: String): CarbonReportThreshold {
    return CarbonReportThreshold(
        code = code,
        title = title,
        minimum = 0.0,
        windowDays = 0,
        region = "",
        status = CarbonReportThresholdStatus.IDLE,
    )
}
