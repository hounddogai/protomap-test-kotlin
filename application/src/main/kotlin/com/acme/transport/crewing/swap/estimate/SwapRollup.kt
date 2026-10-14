package com.acme.transport.crewing.swap.estimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap rollup is in its life cycle. */
enum class SwapRollupStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a swap rollup and its current status. */
data class SwapRollup(
    var code: String,
    var name: String,
    var channel: String,
    var fuelLiters: Double,
    var quantity: Int,
    var cost: Double,
    var trailerId: String,
    var pinned: Boolean,
    var description: String,
    var unit: String,
    var status: SwapRollupStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapRollupStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapRollupStatus): Boolean {
        when (status) {
            SwapRollupStatus.DRAFT -> {
                when (next) {
                    SwapRollupStatus.CLOSED -> return true
                }
                return false
            }
            SwapRollupStatus.PENDING -> return next == SwapRollupStatus.SUSPENDED
            SwapRollupStatus.ACTIVE -> {
                when (next) {
                    SwapRollupStatus.CLOSED -> return true
                }
                return false
            }
            SwapRollupStatus.SUSPENDED -> return next == SwapRollupStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SwapRollupStatus) {
        if (!canMoveTo(next)) {
            error("swap rollup $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun netVolume(): Double = round(fuelLiters * cost * 100) / 100

    fun snapshot(): SwapRollup {
        return SwapRollup(
            code = code,
            name = name,
            channel = channel,
            fuelLiters = fuelLiters,
            quantity = quantity,
            cost = cost,
            trailerId = trailerId,
            pinned = pinned,
            description = description,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a swap rollup in status draft. */
fun newSwapRollup(code: String, name: String): SwapRollup {
    return SwapRollup(
        code = code,
        name = name,
        channel = "",
        fuelLiters = 0.0,
        quantity = 0,
        cost = 0.0,
        trailerId = "",
        pinned = false,
        description = "",
        unit = "",
        status = SwapRollupStatus.DRAFT,
    )
}
