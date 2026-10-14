package com.acme.transport.emissions.carbonreport.rollup

import com.acme.transport.kit.text.normalize

// The carbon report rollup records of emissions's carbon report work, and the rules, stores, and calculations
// around them.

/** Where a carbon report rollup is in its life cycle. */
enum class CarbonReportRollupStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A carbon report rollup as the carbon report team keeps it. */
data class CarbonReportRollup(
    var code: String,
    var label: String,
    var lane: String,
    var country: String,
    var source: String,
    var routeCode: String,
    var expiresAt: Int,
    var summary: String,
    var status: CarbonReportRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportRollupStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportRollupStatus): Boolean {
        when (status) {
            CarbonReportRollupStatus.REQUESTED -> return next == CarbonReportRollupStatus.REVOKED
            CarbonReportRollupStatus.GRANTED -> return next == CarbonReportRollupStatus.EXPIRED
            CarbonReportRollupStatus.REVOKED -> return next == CarbonReportRollupStatus.EXPIRED
        }
        return false
    }

    fun moveTo(next: CarbonReportRollupStatus) {
        if (!canMoveTo(next)) {
            error("carbon report rollup $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = expiresAt.toDouble()

    fun snapshot(): CarbonReportRollup {
        return CarbonReportRollup(
            code = code,
            label = label,
            lane = lane,
            country = country,
            source = source,
            routeCode = routeCode,
            expiresAt = expiresAt,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a carbon report rollup in status requested. */
fun newCarbonReportRollup(code: String, label: String): CarbonReportRollup {
    return CarbonReportRollup(
        code = code,
        label = label,
        lane = "",
        country = "",
        source = "",
        routeCode = "",
        expiresAt = 0,
        summary = "",
        status = CarbonReportRollupStatus.REQUESTED,
    )
}
