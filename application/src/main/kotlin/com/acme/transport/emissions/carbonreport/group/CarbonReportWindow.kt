package com.acme.transport.emissions.carbonreport.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a carbon report window is in its life cycle. */
enum class CarbonReportWindowStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a carbon report window and its current status. */
data class CarbonReportWindow(
    var code: String,
    var label: String,
    var durationMinutes: Int,
    var distanceKm: Double,
    var pinned: Boolean,
    var channel: String,
    var carrier: String,
    var ratio: Double,
    var checksum: String,
    var status: CarbonReportWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportWindowStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportWindowStatus): Boolean {
        when (status) {
            CarbonReportWindowStatus.PROPOSED -> return next == CarbonReportWindowStatus.APPROVED || next == CarbonReportWindowStatus.REJECTED
            CarbonReportWindowStatus.APPROVED -> {
                when (next) {
                    CarbonReportWindowStatus.REJECTED, CarbonReportWindowStatus.WITHDRAWN -> return true
                }
                return false
            }
            CarbonReportWindowStatus.REJECTED -> return next == CarbonReportWindowStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: CarbonReportWindowStatus) {
        if (!canMoveTo(next)) {
            error("carbon report window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedLoad(): Double = round(distanceKm * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): CarbonReportWindow {
        return CarbonReportWindow(
            code = code,
            label = label,
            durationMinutes = durationMinutes,
            distanceKm = distanceKm,
            pinned = pinned,
            channel = channel,
            carrier = carrier,
            ratio = ratio,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a carbon report window in status proposed. */
fun newCarbonReportWindow(code: String, label: String): CarbonReportWindow {
    return CarbonReportWindow(
        code = code,
        label = label,
        durationMinutes = 0,
        distanceKm = 0.0,
        pinned = false,
        channel = "",
        carrier = "",
        ratio = 0.0,
        checksum = "",
        status = CarbonReportWindowStatus.PROPOSED,
    )
}
