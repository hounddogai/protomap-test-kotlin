package com.acme.transport.crewing.swap.adjustment

import com.acme.transport.kit.text.normalize

/** Where a swap tier is in its life cycle. */
enum class SwapTierStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a swap tier and its current status. */
data class SwapTier(
    var code: String,
    var title: String,
    var depot: String,
    var retries: Int,
    var region: String,
    var status: SwapTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapTierStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapTierStatus): Boolean {
        when (status) {
            SwapTierStatus.IDLE -> {
                when (next) {
                    SwapTierStatus.STOPPED -> return true
                }
                return false
            }
            SwapTierStatus.WARMING -> {
                when (next) {
                    SwapTierStatus.DRAINING, SwapTierStatus.STOPPED -> return true
                }
                return false
            }
            SwapTierStatus.READY -> return next == SwapTierStatus.STOPPED
            SwapTierStatus.DRAINING -> return next == SwapTierStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: SwapTierStatus) {
        if (!canMoveTo(next)) {
            error("swap tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"swap tier"} $code"

    fun measuredValue(): Double = retries.toDouble()

    fun snapshot(): SwapTier {
        return SwapTier(
            code = code,
            title = title,
            depot = depot,
            retries = retries,
            region = region,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a swap tier in status idle. */
fun newSwapTier(code: String, title: String): SwapTier {
    return SwapTier(
        code = code,
        title = title,
        depot = "",
        retries = 0,
        region = "",
        status = SwapTierStatus.IDLE,
    )
}
