package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a redelivery variance is in its life cycle. */
enum class RedeliveryVarianceStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One redelivery variance, with what the planners need to act on it. */
data class RedeliveryVariance(
    var code: String,
    var title: String,
    var stopCount: Int,
    var unit: String,
    var attempts: Int,
    var maximum: Double,
    var quantity: Int,
    var pinned: Boolean,
    var externalRef: String,
    var shiftCode: String,
    var status: RedeliveryVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryVarianceStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryVarianceStatus): Boolean {
        when (status) {
            RedeliveryVarianceStatus.IDLE -> return next == RedeliveryVarianceStatus.DRAINING
            RedeliveryVarianceStatus.WARMING -> {
                when (next) {
                    RedeliveryVarianceStatus.READY -> return true
                }
                return false
            }
            RedeliveryVarianceStatus.READY -> return next == RedeliveryVarianceStatus.DRAINING || next == RedeliveryVarianceStatus.STOPPED
            RedeliveryVarianceStatus.DRAINING -> {
                when (next) {
                    RedeliveryVarianceStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RedeliveryVarianceStatus) {
        if (!canMoveTo(next)) {
            error("redelivery variance $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedLoad(): Double = round(attempts.toDouble() * maximum * 100) / 100

    fun snapshot(): RedeliveryVariance {
        return RedeliveryVariance(
            code = code,
            title = title,
            stopCount = stopCount,
            unit = unit,
            attempts = attempts,
            maximum = maximum,
            quantity = quantity,
            pinned = pinned,
            externalRef = externalRef,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a redelivery variance in status idle. */
fun newRedeliveryVariance(code: String, title: String): RedeliveryVariance {
    return RedeliveryVariance(
        code = code,
        title = title,
        stopCount = 0,
        unit = "",
        attempts = 0,
        maximum = 0.0,
        quantity = 0,
        pinned = false,
        externalRef = "",
        shiftCode = "",
        status = RedeliveryVarianceStatus.IDLE,
    )
}
