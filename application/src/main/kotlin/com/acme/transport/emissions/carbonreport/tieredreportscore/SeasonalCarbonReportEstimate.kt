package com.acme.transport.emissions.carbonreport.tieredreportscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal carbon report estimate is in its life cycle. */
enum class SeasonalCarbonReportEstimateStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One seasonal carbon report estimate, with what the planners need to act on it. */
data class SeasonalCarbonReportEstimate(
    var code: String,
    var name: String,
    var attempts: Int,
    var routeCode: String,
    var windowDays: Int,
    var checksum: String,
    var ownerId: String,
    var segment: String,
    var status: SeasonalCarbonReportEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCarbonReportEstimateStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCarbonReportEstimateStatus): Boolean {
        when (status) {
            SeasonalCarbonReportEstimateStatus.DRAFT -> {
                when (next) {
                    SeasonalCarbonReportEstimateStatus.ACTIVE -> return true
                }
                return false
            }
            SeasonalCarbonReportEstimateStatus.PENDING -> {
                when (next) {
                    SeasonalCarbonReportEstimateStatus.SUSPENDED, SeasonalCarbonReportEstimateStatus.CLOSED -> return true
                }
                return false
            }
            SeasonalCarbonReportEstimateStatus.ACTIVE -> return next == SeasonalCarbonReportEstimateStatus.CLOSED
            SeasonalCarbonReportEstimateStatus.SUSPENDED -> {
                when (next) {
                    SeasonalCarbonReportEstimateStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalCarbonReportEstimateStatus) {
        if (!canMoveTo(next)) {
            error("seasonal carbon report estimate $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netLoad(): Double = round((windowDays.toDouble() - attempts.toDouble()) * 100) / 100

    fun snapshot(): SeasonalCarbonReportEstimate {
        return SeasonalCarbonReportEstimate(
            code = code,
            name = name,
            attempts = attempts,
            routeCode = routeCode,
            windowDays = windowDays,
            checksum = checksum,
            ownerId = ownerId,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(segment)
}

/** Starts a seasonal carbon report estimate in status draft. */
fun newSeasonalCarbonReportEstimate(code: String, name: String): SeasonalCarbonReportEstimate {
    return SeasonalCarbonReportEstimate(
        code = code,
        name = name,
        attempts = 0,
        routeCode = "",
        windowDays = 0,
        checksum = "",
        ownerId = "",
        segment = "",
        status = SeasonalCarbonReportEstimateStatus.DRAFT,
    )
}
