package com.acme.transport.freightpay.fuelsurcharge.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fuel surcharge cycle is in its life cycle. */
enum class FuelSurchargeCycleStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A fuel surcharge cycle as the fuel surcharge team keeps it. */
data class FuelSurchargeCycle(
    var code: String,
    var name: String,
    var windowDays: Int,
    var visible: Boolean,
    var unit: String,
    var title: String,
    var floorValue: Double,
    var startsAt: Int,
    var routeCode: String,
    var status: FuelSurchargeCycleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelSurchargeCycleStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelSurchargeCycleStatus): Boolean {
        when (status) {
            FuelSurchargeCycleStatus.OPEN -> return next == FuelSurchargeCycleStatus.RESOLVED
            FuelSurchargeCycleStatus.IN_REVIEW -> {
                when (next) {
                    FuelSurchargeCycleStatus.RESOLVED, FuelSurchargeCycleStatus.REOPENED -> return true
                }
                return false
            }
            FuelSurchargeCycleStatus.RESOLVED -> {
                when (next) {
                    FuelSurchargeCycleStatus.REOPENED -> return true
                }
                return false
            }
            FuelSurchargeCycleStatus.REOPENED -> {
                when (next) {
                    FuelSurchargeCycleStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelSurchargeCycleStatus) {
        if (!canMoveTo(next)) {
            error("fuel surcharge cycle $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $name"

    fun netExposure(): Double = round(floorValue * windowDays.toDouble() * 100) / 100

    fun snapshot(): FuelSurchargeCycle {
        return FuelSurchargeCycle(
            code = code,
            name = name,
            windowDays = windowDays,
            visible = visible,
            unit = unit,
            title = title,
            floorValue = floorValue,
            startsAt = startsAt,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fuel surcharge cycle in status open. */
fun newFuelSurchargeCycle(code: String, name: String): FuelSurchargeCycle {
    return FuelSurchargeCycle(
        code = code,
        name = name,
        windowDays = 0,
        visible = false,
        unit = "",
        title = "",
        floorValue = 0.0,
        startsAt = 0,
        routeCode = "",
        status = FuelSurchargeCycleStatus.OPEN,
    )
}
