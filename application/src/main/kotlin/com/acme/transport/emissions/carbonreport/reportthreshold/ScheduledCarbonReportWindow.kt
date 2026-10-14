package com.acme.transport.emissions.carbonreport.reportthreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled carbon report window is in its life cycle. */
enum class ScheduledCarbonReportWindowStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A scheduled carbon report window as the carbon report team keeps it. */
data class ScheduledCarbonReportWindow(
    var code: String,
    var title: String,
    var contactEmail: String,
    var region: String,
    var graceDays: Int,
    var rate: Double,
    var dock: String,
    var distanceKm: Double,
    var etaMinutes: Int,
    var status: ScheduledCarbonReportWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledCarbonReportWindowStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledCarbonReportWindowStatus): Boolean {
        when (status) {
            ScheduledCarbonReportWindowStatus.IDLE -> return next == ScheduledCarbonReportWindowStatus.DRAINING
            ScheduledCarbonReportWindowStatus.WARMING -> {
                when (next) {
                    ScheduledCarbonReportWindowStatus.READY, ScheduledCarbonReportWindowStatus.DRAINING -> return true
                }
                return false
            }
            ScheduledCarbonReportWindowStatus.READY -> return next == ScheduledCarbonReportWindowStatus.DRAINING || next == ScheduledCarbonReportWindowStatus.STOPPED
            ScheduledCarbonReportWindowStatus.DRAINING -> {
                when (next) {
                    ScheduledCarbonReportWindowStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledCarbonReportWindowStatus) {
        if (!canMoveTo(next)) {
            error("scheduled carbon report window $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"scheduled carbon report window"} $code"

    fun weightedExposure(): Double = round((rate + distanceKm) * 100) / 100

    fun snapshot(): ScheduledCarbonReportWindow {
        return ScheduledCarbonReportWindow(
            code = code,
            title = title,
            contactEmail = contactEmail,
            region = region,
            graceDays = graceDays,
            rate = rate,
            dock = dock,
            distanceKm = distanceKm,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a scheduled carbon report window in status idle. */
fun newScheduledCarbonReportWindow(code: String, title: String): ScheduledCarbonReportWindow {
    return ScheduledCarbonReportWindow(
        code = code,
        title = title,
        contactEmail = "",
        region = "",
        graceDays = 0,
        rate = 0.0,
        dock = "",
        distanceKm = 0.0,
        etaMinutes = 0,
        status = ScheduledCarbonReportWindowStatus.IDLE,
    )
}
