package com.acme.transport.crewing.swap.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap override is in its life cycle. */
enum class SwapOverrideStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A swap override as the swap team keeps it. */
data class SwapOverride(
    var code: String,
    var name: String,
    var archived: Boolean,
    var ratio: Double,
    var fuelLiters: Double,
    var visible: Boolean,
    var maximum: Double,
    var etaMinutes: Int,
    var quantity: Int,
    var depot: String,
    var odometer: Int,
    var status: SwapOverrideStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapOverrideStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapOverrideStatus): Boolean {
        when (status) {
            SwapOverrideStatus.REQUESTED -> return next == SwapOverrideStatus.GRANTED || next == SwapOverrideStatus.REVOKED
            SwapOverrideStatus.GRANTED -> return next == SwapOverrideStatus.REVOKED
            SwapOverrideStatus.REVOKED -> {
                when (next) {
                    SwapOverrideStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapOverrideStatus) {
        if (!canMoveTo(next)) {
            error("swap override $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun weightedVolume(): Double = round((etaMinutes.toDouble() + maximum) * 100) / 100

    fun snapshot(): SwapOverride {
        return SwapOverride(
            code = code,
            name = name,
            archived = archived,
            ratio = ratio,
            fuelLiters = fuelLiters,
            visible = visible,
            maximum = maximum,
            etaMinutes = etaMinutes,
            quantity = quantity,
            depot = depot,
            odometer = odometer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a swap override in status requested. */
fun newSwapOverride(code: String, name: String): SwapOverride {
    return SwapOverride(
        code = code,
        name = name,
        archived = false,
        ratio = 0.0,
        fuelLiters = 0.0,
        visible = false,
        maximum = 0.0,
        etaMinutes = 0,
        quantity = 0,
        depot = "",
        odometer = 0,
        status = SwapOverrideStatus.REQUESTED,
    )
}
