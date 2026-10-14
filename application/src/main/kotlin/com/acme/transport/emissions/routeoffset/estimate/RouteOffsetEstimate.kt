package com.acme.transport.emissions.routeoffset.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The route offset estimate records of emissions's route offset work, and the rules, stores, and calculations
// around them.

/** Where a route offset estimate is in its life cycle. */
enum class RouteOffsetEstimateStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a route offset estimate and its current status. */
data class RouteOffsetEstimate(
    var code: String,
    var name: String,
    var lane: String,
    var etaMinutes: Int,
    var minimum: Double,
    var discount: Double,
    var batchSize: Int,
    var pallets: Int,
    var updatedAt: Int,
    var category: String,
    var status: RouteOffsetEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RouteOffsetEstimateStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: RouteOffsetEstimateStatus): Boolean {
        when (status) {
            RouteOffsetEstimateStatus.CREATED -> return next == RouteOffsetEstimateStatus.VALIDATED || next == RouteOffsetEstimateStatus.PUBLISHED
            RouteOffsetEstimateStatus.VALIDATED -> return next == RouteOffsetEstimateStatus.RETIRED
            RouteOffsetEstimateStatus.PUBLISHED -> {
                when (next) {
                    RouteOffsetEstimateStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RouteOffsetEstimateStatus) {
        if (!canMoveTo(next)) {
            error("route offset estimate $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"route offset estimate"} $code"

    fun netTotal(): Double = round(discount * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): RouteOffsetEstimate {
        return RouteOffsetEstimate(
            code = code,
            name = name,
            lane = lane,
            etaMinutes = etaMinutes,
            minimum = minimum,
            discount = discount,
            batchSize = batchSize,
            pallets = pallets,
            updatedAt = updatedAt,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a route offset estimate in status created. */
fun newRouteOffsetEstimate(code: String, name: String): RouteOffsetEstimate {
    return RouteOffsetEstimate(
        code = code,
        name = name,
        lane = "",
        etaMinutes = 0,
        minimum = 0.0,
        discount = 0.0,
        batchSize = 0,
        pallets = 0,
        updatedAt = 0,
        category = "",
        status = RouteOffsetEstimateStatus.CREATED,
    )
}
