package com.acme.transport.emissions.carbonreport.reportestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted carbon report estimate records of emissions's carbon report work, and the rules, stores, and
// calculations around them.

/** Where a weighted carbon report estimate is in its life cycle. */
enum class WeightedCarbonReportEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A weighted carbon report estimate as the carbon report team keeps it. */
data class WeightedCarbonReportEstimate(
    var code: String,
    var name: String,
    var routeCode: String,
    var retries: Int,
    var updatedAt: Int,
    var depot: String,
    var quantity: Int,
    var shiftCode: String,
    var lane: String,
    var windowDays: Int,
    var status: WeightedCarbonReportEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCarbonReportEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCarbonReportEstimateStatus): Boolean {
        when (status) {
            WeightedCarbonReportEstimateStatus.IDLE -> return next == WeightedCarbonReportEstimateStatus.WARMING || next == WeightedCarbonReportEstimateStatus.DRAINING
            WeightedCarbonReportEstimateStatus.WARMING -> {
                when (next) {
                    WeightedCarbonReportEstimateStatus.READY, WeightedCarbonReportEstimateStatus.DRAINING -> return true
                }
                return false
            }
            WeightedCarbonReportEstimateStatus.READY -> {
                when (next) {
                    WeightedCarbonReportEstimateStatus.DRAINING, WeightedCarbonReportEstimateStatus.STOPPED -> return true
                }
                return false
            }
            WeightedCarbonReportEstimateStatus.DRAINING -> {
                when (next) {
                    WeightedCarbonReportEstimateStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedCarbonReportEstimateStatus) {
        if (!canMoveTo(next)) {
            error("weighted carbon report estimate $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun weightedExposure(): Double = round((retries.toDouble() - updatedAt.toDouble()) * 100) / 100

    fun snapshot(): WeightedCarbonReportEstimate {
        return WeightedCarbonReportEstimate(
            code = code,
            name = name,
            routeCode = routeCode,
            retries = retries,
            updatedAt = updatedAt,
            depot = depot,
            quantity = quantity,
            shiftCode = shiftCode,
            lane = lane,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a weighted carbon report estimate in status idle. */
fun newWeightedCarbonReportEstimate(code: String, name: String): WeightedCarbonReportEstimate {
    return WeightedCarbonReportEstimate(
        code = code,
        name = name,
        routeCode = "",
        retries = 0,
        updatedAt = 0,
        depot = "",
        quantity = 0,
        shiftCode = "",
        lane = "",
        windowDays = 0,
        status = WeightedCarbonReportEstimateStatus.IDLE,
    )
}
