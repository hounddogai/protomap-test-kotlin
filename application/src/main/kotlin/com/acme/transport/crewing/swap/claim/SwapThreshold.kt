package com.acme.transport.crewing.swap.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap threshold is in its life cycle. */
enum class SwapThresholdStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** One swap threshold, with what the planners need to act on it. */
data class SwapThreshold(
    var code: String,
    var title: String,
    var channel: String,
    var discount: Double,
    var segment: String,
    var locale: String,
    var sequence: Int,
    var rate: Double,
    var pallets: Int,
    var status: SwapThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapThresholdStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapThresholdStatus): Boolean {
        when (status) {
            SwapThresholdStatus.PLANNED -> {
                when (next) {
                    SwapThresholdStatus.IN_PROGRESS -> return true
                }
                return false
            }
            SwapThresholdStatus.SCHEDULED -> {
                when (next) {
                    SwapThresholdStatus.COMPLETED, SwapThresholdStatus.CANCELLED -> return true
                }
                return false
            }
            SwapThresholdStatus.IN_PROGRESS -> return next == SwapThresholdStatus.CANCELLED
            SwapThresholdStatus.COMPLETED -> {
                when (next) {
                    SwapThresholdStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapThresholdStatus) {
        if (!canMoveTo(next)) {
            error("swap threshold $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun weightedVolume(): Double = round(discount * sequence.toDouble() * 100) / 100

    fun snapshot(): SwapThreshold {
        return SwapThreshold(
            code = code,
            title = title,
            channel = channel,
            discount = discount,
            segment = segment,
            locale = locale,
            sequence = sequence,
            rate = rate,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a swap threshold in status planned. */
fun newSwapThreshold(code: String, title: String): SwapThreshold {
    return SwapThreshold(
        code = code,
        title = title,
        channel = "",
        discount = 0.0,
        segment = "",
        locale = "",
        sequence = 0,
        rate = 0.0,
        pallets = 0,
        status = SwapThresholdStatus.PLANNED,
    )
}
