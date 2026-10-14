package com.acme.transport.emissions.routeoffset.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal route offset adjustment is in its life cycle. */
enum class SeasonalRouteOffsetAdjustmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A seasonal route offset adjustment as the route offset team keeps it. */
data class SeasonalRouteOffsetAdjustment(
    var code: String,
    var name: String,
    var createdAt: Int,
    var rate: Double,
    var stopCount: Int,
    var routeCode: String,
    var currency: String,
    var windowDays: Int,
    var discount: Double,
    var ratio: Double,
    var status: SeasonalRouteOffsetAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRouteOffsetAdjustmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRouteOffsetAdjustmentStatus): Boolean {
        when (status) {
            SeasonalRouteOffsetAdjustmentStatus.IDLE -> {
                when (next) {
                    SeasonalRouteOffsetAdjustmentStatus.WARMING, SeasonalRouteOffsetAdjustmentStatus.READY -> return true
                }
                return false
            }
            SeasonalRouteOffsetAdjustmentStatus.WARMING -> {
                when (next) {
                    SeasonalRouteOffsetAdjustmentStatus.READY, SeasonalRouteOffsetAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalRouteOffsetAdjustmentStatus.READY -> return next == SeasonalRouteOffsetAdjustmentStatus.DRAINING || next == SeasonalRouteOffsetAdjustmentStatus.STOPPED
            SeasonalRouteOffsetAdjustmentStatus.DRAINING -> {
                when (next) {
                    SeasonalRouteOffsetAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalRouteOffsetAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("seasonal route offset adjustment $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($name)"

    fun effectiveExposure(): Double = round(createdAt.toDouble() * ratio * 100) / 100

    fun snapshot(): SeasonalRouteOffsetAdjustment {
        return SeasonalRouteOffsetAdjustment(
            code = code,
            name = name,
            createdAt = createdAt,
            rate = rate,
            stopCount = stopCount,
            routeCode = routeCode,
            currency = currency,
            windowDays = windowDays,
            discount = discount,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a seasonal route offset adjustment in status idle. */
fun newSeasonalRouteOffsetAdjustment(code: String, name: String): SeasonalRouteOffsetAdjustment {
    return SeasonalRouteOffsetAdjustment(
        code = code,
        name = name,
        createdAt = 0,
        rate = 0.0,
        stopCount = 0,
        routeCode = "",
        currency = "",
        windowDays = 0,
        discount = 0.0,
        ratio = 0.0,
        status = SeasonalRouteOffsetAdjustmentStatus.IDLE,
    )
}
