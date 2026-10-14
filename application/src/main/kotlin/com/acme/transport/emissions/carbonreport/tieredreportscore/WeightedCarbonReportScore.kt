package com.acme.transport.emissions.carbonreport.tieredreportscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted carbon report score records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a weighted carbon report score is in its life cycle. */
enum class WeightedCarbonReportScoreStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a weighted carbon report score and its current status. */
data class WeightedCarbonReportScore(
    var code: String,
    var label: String,
    var startsAt: Int,
    var expiresAt: Int,
    var shiftCode: String,
    var status: WeightedCarbonReportScoreStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCarbonReportScoreStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCarbonReportScoreStatus): Boolean {
        when (status) {
            WeightedCarbonReportScoreStatus.PLANNED -> {
                when (next) {
                    WeightedCarbonReportScoreStatus.SCHEDULED -> return true
                }
                return false
            }
            WeightedCarbonReportScoreStatus.SCHEDULED -> return next == WeightedCarbonReportScoreStatus.CANCELLED
            WeightedCarbonReportScoreStatus.IN_PROGRESS -> return next == WeightedCarbonReportScoreStatus.COMPLETED || next == WeightedCarbonReportScoreStatus.CANCELLED
            WeightedCarbonReportScoreStatus.COMPLETED -> {
                when (next) {
                    WeightedCarbonReportScoreStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedCarbonReportScoreStatus) {
        if (!canMoveTo(next)) {
            error("weighted carbon report score $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveVolume(): Double = round((expiresAt.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedCarbonReportScore {
        return WeightedCarbonReportScore(
            code = code,
            label = label,
            startsAt = startsAt,
            expiresAt = expiresAt,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a weighted carbon report score in status planned. */
fun newWeightedCarbonReportScore(code: String, label: String): WeightedCarbonReportScore {
    return WeightedCarbonReportScore(
        code = code,
        label = label,
        startsAt = 0,
        expiresAt = 0,
        shiftCode = "",
        status = WeightedCarbonReportScoreStatus.PLANNED,
    )
}
