package com.acme.transport.lastmile.attempt.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a attempt adjustment is in its life cycle. */
enum class AttemptAdjustmentStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One attempt adjustment, with what the planners need to act on it. */
data class AttemptAdjustment(
    var code: String,
    var label: String,
    var channel: String,
    var ceiling: Double,
    var routeCode: String,
    var quantity: Int,
    var source: String,
    var stopCount: Int,
    var ownerId: String,
    var amount: Double,
    var status: AttemptAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AttemptAdjustmentStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AttemptAdjustmentStatus): Boolean {
        when (status) {
            AttemptAdjustmentStatus.PROPOSED -> return next == AttemptAdjustmentStatus.WITHDRAWN
            AttemptAdjustmentStatus.APPROVED -> return next == AttemptAdjustmentStatus.WITHDRAWN
            AttemptAdjustmentStatus.REJECTED -> return next == AttemptAdjustmentStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: AttemptAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("attempt adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun adjustedLoad(): Double = round((quantity.toDouble() + stopCount.toDouble()) * 100) / 100

    fun snapshot(): AttemptAdjustment {
        return AttemptAdjustment(
            code = code,
            label = label,
            channel = channel,
            ceiling = ceiling,
            routeCode = routeCode,
            quantity = quantity,
            source = source,
            stopCount = stopCount,
            ownerId = ownerId,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a attempt adjustment in status proposed. */
fun newAttemptAdjustment(code: String, label: String): AttemptAdjustment {
    return AttemptAdjustment(
        code = code,
        label = label,
        channel = "",
        ceiling = 0.0,
        routeCode = "",
        quantity = 0,
        source = "",
        stopCount = 0,
        ownerId = "",
        amount = 0.0,
        status = AttemptAdjustmentStatus.PROPOSED,
    )
}
