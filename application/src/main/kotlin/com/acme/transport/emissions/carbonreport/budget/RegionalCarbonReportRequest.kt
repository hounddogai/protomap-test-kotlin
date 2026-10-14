package com.acme.transport.emissions.carbonreport.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional carbon report request is in its life cycle. */
enum class RegionalCarbonReportRequestStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a regional carbon report request and its current status. */
data class RegionalCarbonReportRequest(
    var code: String,
    var title: String,
    var expiresAt: Int,
    var approved: Boolean,
    var startsAt: Int,
    var source: String,
    var retries: Int,
    var odometer: Int,
    var status: RegionalCarbonReportRequestStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCarbonReportRequestStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCarbonReportRequestStatus): Boolean {
        when (status) {
            RegionalCarbonReportRequestStatus.IDLE -> return next == RegionalCarbonReportRequestStatus.WARMING || next == RegionalCarbonReportRequestStatus.DRAINING
            RegionalCarbonReportRequestStatus.WARMING -> return next == RegionalCarbonReportRequestStatus.STOPPED
            RegionalCarbonReportRequestStatus.READY -> return next == RegionalCarbonReportRequestStatus.DRAINING || next == RegionalCarbonReportRequestStatus.STOPPED
            RegionalCarbonReportRequestStatus.DRAINING -> {
                when (next) {
                    RegionalCarbonReportRequestStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalCarbonReportRequestStatus) {
        if (!canMoveTo(next)) {
            error("regional carbon report request $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun effectiveTotal(): Double = round((expiresAt.toDouble() - retries.toDouble()) * 100) / 100

    fun snapshot(): RegionalCarbonReportRequest {
        return RegionalCarbonReportRequest(
            code = code,
            title = title,
            expiresAt = expiresAt,
            approved = approved,
            startsAt = startsAt,
            source = source,
            retries = retries,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a regional carbon report request in status idle. */
fun newRegionalCarbonReportRequest(code: String, title: String): RegionalCarbonReportRequest {
    return RegionalCarbonReportRequest(
        code = code,
        title = title,
        expiresAt = 0,
        approved = false,
        startsAt = 0,
        source = "",
        retries = 0,
        odometer = 0,
        status = RegionalCarbonReportRequestStatus.IDLE,
    )
}
