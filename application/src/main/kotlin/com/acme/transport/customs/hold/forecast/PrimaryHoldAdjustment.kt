package com.acme.transport.customs.hold.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a primary hold adjustment is in its life cycle. */
enum class PrimaryHoldAdjustmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A primary hold adjustment. Its code is unique within a tenant. */
data class PrimaryHoldAdjustment(
    var code: String,
    var name: String,
    var stopCount: Int,
    var routeCode: String,
    var vehicleId: String,
    var country: String,
    var channel: String,
    var threshold: Double,
    var currency: String,
    var status: PrimaryHoldAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PrimaryHoldAdjustmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: PrimaryHoldAdjustmentStatus): Boolean {
        when (status) {
            PrimaryHoldAdjustmentStatus.IDLE -> return next == PrimaryHoldAdjustmentStatus.WARMING
            PrimaryHoldAdjustmentStatus.WARMING -> return next == PrimaryHoldAdjustmentStatus.READY
            PrimaryHoldAdjustmentStatus.READY -> return next == PrimaryHoldAdjustmentStatus.DRAINING
            PrimaryHoldAdjustmentStatus.DRAINING -> {
                when (next) {
                    PrimaryHoldAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PrimaryHoldAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("primary hold adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"primary hold adjustment"} $code"

    fun projectedLoad(): Double = round((stopCount.toDouble() - threshold) * 100) / 100

    fun snapshot(): PrimaryHoldAdjustment {
        return PrimaryHoldAdjustment(
            code = code,
            name = name,
            stopCount = stopCount,
            routeCode = routeCode,
            vehicleId = vehicleId,
            country = country,
            channel = channel,
            threshold = threshold,
            currency = currency,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a primary hold adjustment in status idle. */
fun newPrimaryHoldAdjustment(code: String, name: String): PrimaryHoldAdjustment {
    return PrimaryHoldAdjustment(
        code = code,
        name = name,
        stopCount = 0,
        routeCode = "",
        vehicleId = "",
        country = "",
        channel = "",
        threshold = 0.0,
        currency = "",
        status = PrimaryHoldAdjustmentStatus.IDLE,
    )
}
