package com.acme.transport.lastmile.redelivery.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback redelivery tier is in its life cycle. */
enum class FallbackRedeliveryTierStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** The record of a fallback redelivery tier and its current status. */
data class FallbackRedeliveryTier(
    var code: String,
    var label: String,
    var maximum: Double,
    var depot: String,
    var limit: Int,
    var sequence: Int,
    var region: String,
    var driverId: String,
    var version: Int,
    var status: FallbackRedeliveryTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackRedeliveryTierStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackRedeliveryTierStatus): Boolean {
        when (status) {
            FallbackRedeliveryTierStatus.IDLE -> return next == FallbackRedeliveryTierStatus.STOPPED
            FallbackRedeliveryTierStatus.WARMING -> return next == FallbackRedeliveryTierStatus.READY || next == FallbackRedeliveryTierStatus.DRAINING
            FallbackRedeliveryTierStatus.READY -> return next == FallbackRedeliveryTierStatus.DRAINING || next == FallbackRedeliveryTierStatus.STOPPED
            FallbackRedeliveryTierStatus.DRAINING -> return next == FallbackRedeliveryTierStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: FallbackRedeliveryTierStatus) {
        if (!canMoveTo(next)) {
            error("fallback redelivery tier $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun displayLabel(): String = "${"fallback redelivery tier"} $code"

    fun projectedValue(): Double = round(version.toDouble() * sequence.toDouble() * 100) / 100

    fun snapshot(): FallbackRedeliveryTier {
        return FallbackRedeliveryTier(
            code = code,
            label = label,
            maximum = maximum,
            depot = depot,
            limit = limit,
            sequence = sequence,
            region = region,
            driverId = driverId,
            version = version,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a fallback redelivery tier in status idle. */
fun newFallbackRedeliveryTier(code: String, label: String): FallbackRedeliveryTier {
    return FallbackRedeliveryTier(
        code = code,
        label = label,
        maximum = 0.0,
        depot = "",
        limit = 0,
        sequence = 0,
        region = "",
        driverId = "",
        version = 0,
        status = FallbackRedeliveryTierStatus.IDLE,
    )
}
