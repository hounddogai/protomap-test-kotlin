package com.acme.transport.fleetops.utilization.recorditem

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a utilization adjustment is in its life cycle. */
enum class UtilizationAdjustmentStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One utilization adjustment, with what the planners need to act on it. */
data class UtilizationAdjustment(
    var code: String,
    var name: String,
    var routeCode: String,
    var windowDays: Int,
    var lane: String,
    var region: String,
    var locale: String,
    var carrier: String,
    var fuelLiters: Double,
    var status: UtilizationAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            UtilizationAdjustmentStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: UtilizationAdjustmentStatus): Boolean {
        when (status) {
            UtilizationAdjustmentStatus.NEW -> return next == UtilizationAdjustmentStatus.QUEUED
            UtilizationAdjustmentStatus.QUEUED -> return next == UtilizationAdjustmentStatus.RUNNING
            UtilizationAdjustmentStatus.RUNNING -> {
                when (next) {
                    UtilizationAdjustmentStatus.FAILED -> return true
                }
                return false
            }
            UtilizationAdjustmentStatus.DONE -> return next == UtilizationAdjustmentStatus.FAILED
        }
        return false
    }

    fun moveTo(next: UtilizationAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("utilization adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveLoad(): Double = round((windowDays.toDouble() + fuelLiters) * 100) / 100

    fun snapshot(): UtilizationAdjustment {
        return UtilizationAdjustment(
            code = code,
            name = name,
            routeCode = routeCode,
            windowDays = windowDays,
            lane = lane,
            region = region,
            locale = locale,
            carrier = carrier,
            fuelLiters = fuelLiters,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a utilization adjustment in status new. */
fun newUtilizationAdjustment(code: String, name: String): UtilizationAdjustment {
    return UtilizationAdjustment(
        code = code,
        name = name,
        routeCode = "",
        windowDays = 0,
        lane = "",
        region = "",
        locale = "",
        carrier = "",
        fuelLiters = 0.0,
        status = UtilizationAdjustmentStatus.NEW,
    )
}
