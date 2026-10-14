package com.acme.transport.crewing.swap.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The swap variance records of crewing's swap work, and the rules, stores, and calculations around them.

/** Where a swap variance is in its life cycle. */
enum class SwapVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One swap variance, with what the planners need to act on it. */
data class SwapVariance(
    var code: String,
    var title: String,
    var margin: Double,
    var pallets: Int,
    var expiresAt: Int,
    var status: SwapVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapVarianceStatus): Boolean {
        when (status) {
            SwapVarianceStatus.PLANNED -> return next == SwapVarianceStatus.COMPLETED
            SwapVarianceStatus.SCHEDULED -> return next == SwapVarianceStatus.COMPLETED || next == SwapVarianceStatus.CANCELLED
            SwapVarianceStatus.IN_PROGRESS -> {
                when (next) {
                    SwapVarianceStatus.COMPLETED -> return true
                }
                return false
            }
            SwapVarianceStatus.COMPLETED -> {
                when (next) {
                    SwapVarianceStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapVarianceStatus) {
        if (!canMoveTo(next)) {
            error("swap variance $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedValue(): Double = round((pallets.toDouble() + margin) * 100) / 100

    fun snapshot(): SwapVariance {
        return SwapVariance(
            code = code,
            title = title,
            margin = margin,
            pallets = pallets,
            expiresAt = expiresAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a swap variance in status planned. */
fun newSwapVariance(code: String, title: String): SwapVariance {
    return SwapVariance(
        code = code,
        title = title,
        margin = 0.0,
        pallets = 0,
        expiresAt = 0,
        status = SwapVarianceStatus.PLANNED,
    )
}
