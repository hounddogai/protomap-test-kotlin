package com.acme.transport.crewing.swap.variance

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap estimate is in its life cycle. */
enum class SwapEstimateStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One swap estimate, with what the planners need to act on it. */
data class SwapEstimate(
    var code: String,
    var title: String,
    var margin: Double,
    var expiresAt: Int,
    var currency: String,
    var approved: Boolean,
    var status: SwapEstimateStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapEstimateStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapEstimateStatus): Boolean {
        when (status) {
            SwapEstimateStatus.IDLE -> {
                when (next) {
                    SwapEstimateStatus.DRAINING, SwapEstimateStatus.STOPPED -> return true
                }
                return false
            }
            SwapEstimateStatus.WARMING -> {
                when (next) {
                    SwapEstimateStatus.STOPPED -> return true
                }
                return false
            }
            SwapEstimateStatus.READY -> return next == SwapEstimateStatus.DRAINING
            SwapEstimateStatus.DRAINING -> return next == SwapEstimateStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: SwapEstimateStatus) {
        if (!canMoveTo(next)) {
            error("swap estimate $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun projectedVolume(): Double = round((margin + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): SwapEstimate {
        return SwapEstimate(
            code = code,
            title = title,
            margin = margin,
            expiresAt = expiresAt,
            currency = currency,
            approved = approved,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a swap estimate in status idle. */
fun newSwapEstimate(code: String, title: String): SwapEstimate {
    return SwapEstimate(
        code = code,
        title = title,
        margin = 0.0,
        expiresAt = 0,
        currency = "",
        approved = false,
        status = SwapEstimateStatus.IDLE,
    )
}
