package com.acme.transport.network.servicearea.draft

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback service area adjustment is in its life cycle. */
enum class FallbackServiceAreaAdjustmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a fallback service area adjustment and its current status. */
data class FallbackServiceAreaAdjustment(
    var code: String,
    var name: String,
    var maximum: Double,
    var externalRef: String,
    var priority: Int,
    var depot: String,
    var floorValue: Double,
    var carrier: String,
    var quantity: Int,
    var status: FallbackServiceAreaAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackServiceAreaAdjustmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackServiceAreaAdjustmentStatus): Boolean {
        when (status) {
            FallbackServiceAreaAdjustmentStatus.IDLE -> return next == FallbackServiceAreaAdjustmentStatus.READY || next == FallbackServiceAreaAdjustmentStatus.DRAINING
            FallbackServiceAreaAdjustmentStatus.WARMING -> {
                when (next) {
                    FallbackServiceAreaAdjustmentStatus.DRAINING -> return true
                }
                return false
            }
            FallbackServiceAreaAdjustmentStatus.READY -> {
                when (next) {
                    FallbackServiceAreaAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
            FallbackServiceAreaAdjustmentStatus.DRAINING -> {
                when (next) {
                    FallbackServiceAreaAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FallbackServiceAreaAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("fallback service area adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback service area adjustment"} $code"

    fun netVolume(): Double = round((priority.toDouble() - maximum) * 100) / 100

    fun snapshot(): FallbackServiceAreaAdjustment {
        return FallbackServiceAreaAdjustment(
            code = code,
            name = name,
            maximum = maximum,
            externalRef = externalRef,
            priority = priority,
            depot = depot,
            floorValue = floorValue,
            carrier = carrier,
            quantity = quantity,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fallback service area adjustment in status idle. */
fun newFallbackServiceAreaAdjustment(code: String, name: String): FallbackServiceAreaAdjustment {
    return FallbackServiceAreaAdjustment(
        code = code,
        name = name,
        maximum = 0.0,
        externalRef = "",
        priority = 0,
        depot = "",
        floorValue = 0.0,
        carrier = "",
        quantity = 0,
        status = FallbackServiceAreaAdjustmentStatus.IDLE,
    )
}
