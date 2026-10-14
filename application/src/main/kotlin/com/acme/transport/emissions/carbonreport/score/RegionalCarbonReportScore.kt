package com.acme.transport.emissions.carbonreport.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The regional carbon report score records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a regional carbon report score is in its life cycle. */
enum class RegionalCarbonReportScoreStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a regional carbon report score and its current status. */
data class RegionalCarbonReportScore(
    var code: String,
    var title: String,
    var attempts: Int,
    var windowDays: Int,
    var lane: String,
    var minimum: Double,
    var ceiling: Double,
    var maximum: Double,
    var trailerId: String,
    var status: RegionalCarbonReportScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalCarbonReportScoreStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalCarbonReportScoreStatus): Boolean {
        when (status) {
            RegionalCarbonReportScoreStatus.DRAFT -> {
                when (next) {
                    RegionalCarbonReportScoreStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalCarbonReportScoreStatus.PENDING -> {
                when (next) {
                    RegionalCarbonReportScoreStatus.CLOSED -> return true
                }
                return false
            }
            RegionalCarbonReportScoreStatus.ACTIVE -> return next == RegionalCarbonReportScoreStatus.SUSPENDED
            RegionalCarbonReportScoreStatus.SUSPENDED -> return next == RegionalCarbonReportScoreStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RegionalCarbonReportScoreStatus) {
        if (!canMoveTo(next)) {
            error("regional carbon report score $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"regional carbon report score"} $code"

    fun adjustedLoad(): Double = round((maximum + windowDays.toDouble()) * 100) / 100

    fun snapshot(): RegionalCarbonReportScore {
        return RegionalCarbonReportScore(
            code = code,
            title = title,
            attempts = attempts,
            windowDays = windowDays,
            lane = lane,
            minimum = minimum,
            ceiling = ceiling,
            maximum = maximum,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a regional carbon report score in status draft. */
fun newRegionalCarbonReportScore(code: String, title: String): RegionalCarbonReportScore {
    return RegionalCarbonReportScore(
        code = code,
        title = title,
        attempts = 0,
        windowDays = 0,
        lane = "",
        minimum = 0.0,
        ceiling = 0.0,
        maximum = 0.0,
        trailerId = "",
        status = RegionalCarbonReportScoreStatus.DRAFT,
    )
}
