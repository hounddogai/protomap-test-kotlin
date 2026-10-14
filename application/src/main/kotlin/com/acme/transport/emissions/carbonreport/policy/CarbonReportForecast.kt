package com.acme.transport.emissions.carbonreport.policy

import com.acme.transport.kit.text.normalize

/** Where a carbon report forecast is in its life cycle. */
enum class CarbonReportForecastStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** The record of a carbon report forecast and its current status. */
data class CarbonReportForecast(
    var code: String,
    var label: String,
    var reviewer: String,
    var notes: String,
    var routeCode: String,
    var contactEmail: String,
    var description: String,
    var segment: String,
    var score: Double,
    var status: CarbonReportForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CarbonReportForecastStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: CarbonReportForecastStatus): Boolean {
        when (status) {
            CarbonReportForecastStatus.OPEN -> {
                when (next) {
                    CarbonReportForecastStatus.RESOLVED -> return true
                }
                return false
            }
            CarbonReportForecastStatus.IN_REVIEW -> return next == CarbonReportForecastStatus.REOPENED || next == CarbonReportForecastStatus.ARCHIVED
            CarbonReportForecastStatus.RESOLVED -> {
                when (next) {
                    CarbonReportForecastStatus.ARCHIVED -> return true
                }
                return false
            }
            CarbonReportForecastStatus.REOPENED -> return next == CarbonReportForecastStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: CarbonReportForecastStatus) {
        if (!canMoveTo(next)) {
            error("carbon report forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun measuredValue(): Double = score

    fun snapshot(): CarbonReportForecast {
        return CarbonReportForecast(
            code = code,
            label = label,
            reviewer = reviewer,
            notes = notes,
            routeCode = routeCode,
            contactEmail = contactEmail,
            description = description,
            segment = segment,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a carbon report forecast in status open. */
fun newCarbonReportForecast(code: String, label: String): CarbonReportForecast {
    return CarbonReportForecast(
        code = code,
        label = label,
        reviewer = "",
        notes = "",
        routeCode = "",
        contactEmail = "",
        description = "",
        segment = "",
        score = 0.0,
        status = CarbonReportForecastStatus.OPEN,
    )
}
