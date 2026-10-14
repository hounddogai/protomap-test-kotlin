package com.acme.transport.lastmile.redelivery.threshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The redelivery threshold records of lastmile's redelivery work, and the rules, stores, and calculations
// around them.

/** Where a redelivery threshold is in its life cycle. */
enum class RedeliveryThresholdStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a redelivery threshold and its current status. */
data class RedeliveryThreshold(
    var code: String,
    var name: String,
    var minimum: Double,
    var floorValue: Double,
    var expiresAt: Int,
    var category: String,
    var status: RedeliveryThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RedeliveryThresholdStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RedeliveryThresholdStatus): Boolean {
        when (status) {
            RedeliveryThresholdStatus.PROPOSED -> return next == RedeliveryThresholdStatus.REJECTED
            RedeliveryThresholdStatus.APPROVED -> {
                when (next) {
                    RedeliveryThresholdStatus.WITHDRAWN -> return true
                }
                return false
            }
            RedeliveryThresholdStatus.REJECTED -> return next == RedeliveryThresholdStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RedeliveryThresholdStatus) {
        if (!canMoveTo(next)) {
            error("redelivery threshold $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"redelivery threshold"} $code"

    fun adjustedLoad(): Double = round((minimum + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): RedeliveryThreshold {
        return RedeliveryThreshold(
            code = code,
            name = name,
            minimum = minimum,
            floorValue = floorValue,
            expiresAt = expiresAt,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a redelivery threshold in status proposed. */
fun newRedeliveryThreshold(code: String, name: String): RedeliveryThreshold {
    return RedeliveryThreshold(
        code = code,
        name = name,
        minimum = 0.0,
        floorValue = 0.0,
        expiresAt = 0,
        category = "",
        status = RedeliveryThresholdStatus.PROPOSED,
    )
}
