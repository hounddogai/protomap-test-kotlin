package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted accessorial adjustment is in its life cycle. */
enum class WeightedAccessorialAdjustmentStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a weighted accessorial adjustment and its current status. */
data class WeightedAccessorialAdjustment(
    var code: String,
    var name: String,
    var batchSize: Int,
    var currency: String,
    var routeCode: String,
    var amount: Double,
    var status: WeightedAccessorialAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAccessorialAdjustmentStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAccessorialAdjustmentStatus): Boolean {
        when (status) {
            WeightedAccessorialAdjustmentStatus.PLANNED -> return next == WeightedAccessorialAdjustmentStatus.IN_PROGRESS || next == WeightedAccessorialAdjustmentStatus.CANCELLED
            WeightedAccessorialAdjustmentStatus.SCHEDULED -> {
                when (next) {
                    WeightedAccessorialAdjustmentStatus.COMPLETED -> return true
                }
                return false
            }
            WeightedAccessorialAdjustmentStatus.IN_PROGRESS -> return next == WeightedAccessorialAdjustmentStatus.CANCELLED
            WeightedAccessorialAdjustmentStatus.COMPLETED -> return next == WeightedAccessorialAdjustmentStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: WeightedAccessorialAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("weighted accessorial adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun projectedValue(): Double = round(amount * batchSize.toDouble() * 100) / 100

    fun snapshot(): WeightedAccessorialAdjustment {
        return WeightedAccessorialAdjustment(
            code = code,
            name = name,
            batchSize = batchSize,
            currency = currency,
            routeCode = routeCode,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted accessorial adjustment in status planned. */
fun newWeightedAccessorialAdjustment(code: String, name: String): WeightedAccessorialAdjustment {
    return WeightedAccessorialAdjustment(
        code = code,
        name = name,
        batchSize = 0,
        currency = "",
        routeCode = "",
        amount = 0.0,
        status = WeightedAccessorialAdjustmentStatus.PLANNED,
    )
}
