package com.acme.transport.freightpay.fuelsurcharge.mapping

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global fuel surcharge exception is in its life cycle. */
enum class GlobalFuelSurchargeExceptionStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A global fuel surcharge exception as the fuel surcharge team keeps it. */
data class GlobalFuelSurchargeException(
    var code: String,
    var name: String,
    var floorValue: Double,
    var batchSize: Int,
    var vehicleId: String,
    var endsAt: Int,
    var loadKg: Double,
    var routeCode: String,
    var score: Double,
    var status: GlobalFuelSurchargeExceptionStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalFuelSurchargeExceptionStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalFuelSurchargeExceptionStatus): Boolean {
        when (status) {
            GlobalFuelSurchargeExceptionStatus.PLANNED -> {
                when (next) {
                    GlobalFuelSurchargeExceptionStatus.IN_PROGRESS -> return true
                }
                return false
            }
            GlobalFuelSurchargeExceptionStatus.SCHEDULED -> return next == GlobalFuelSurchargeExceptionStatus.COMPLETED || next == GlobalFuelSurchargeExceptionStatus.CANCELLED
            GlobalFuelSurchargeExceptionStatus.IN_PROGRESS -> {
                when (next) {
                    GlobalFuelSurchargeExceptionStatus.COMPLETED -> return true
                }
                return false
            }
            GlobalFuelSurchargeExceptionStatus.COMPLETED -> return next == GlobalFuelSurchargeExceptionStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: GlobalFuelSurchargeExceptionStatus) {
        if (!canMoveTo(next)) {
            error("global fuel surcharge exception $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"global fuel surcharge exception"} $code"

    fun effectiveTotal(): Double = round((score + endsAt.toDouble()) * 100) / 100

    fun snapshot(): GlobalFuelSurchargeException {
        return GlobalFuelSurchargeException(
            code = code,
            name = name,
            floorValue = floorValue,
            batchSize = batchSize,
            vehicleId = vehicleId,
            endsAt = endsAt,
            loadKg = loadKg,
            routeCode = routeCode,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a global fuel surcharge exception in status planned. */
fun newGlobalFuelSurchargeException(code: String, name: String): GlobalFuelSurchargeException {
    return GlobalFuelSurchargeException(
        code = code,
        name = name,
        floorValue = 0.0,
        batchSize = 0,
        vehicleId = "",
        endsAt = 0,
        loadKg = 0.0,
        routeCode = "",
        score = 0.0,
        status = GlobalFuelSurchargeExceptionStatus.PLANNED,
    )
}
