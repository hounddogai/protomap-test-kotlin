package com.acme.transport.fleetops.leasereturn.segment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback lease return tier is in its life cycle. */
enum class FallbackLeaseReturnTierStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a fallback lease return tier and its current status. */
data class FallbackLeaseReturnTier(
    var code: String,
    var name: String,
    var maximum: Double,
    var priority: Int,
    var vehicleId: String,
    var routeCode: String,
    var score: Double,
    var unit: String,
    var capacity: Int,
    var channel: String,
    var segment: String,
    var status: FallbackLeaseReturnTierStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackLeaseReturnTierStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackLeaseReturnTierStatus): Boolean {
        when (status) {
            FallbackLeaseReturnTierStatus.PLANNED -> return next == FallbackLeaseReturnTierStatus.SCHEDULED || next == FallbackLeaseReturnTierStatus.COMPLETED
            FallbackLeaseReturnTierStatus.SCHEDULED -> return next == FallbackLeaseReturnTierStatus.IN_PROGRESS
            FallbackLeaseReturnTierStatus.IN_PROGRESS -> return next == FallbackLeaseReturnTierStatus.COMPLETED
            FallbackLeaseReturnTierStatus.COMPLETED -> return next == FallbackLeaseReturnTierStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: FallbackLeaseReturnTierStatus) {
        if (!canMoveTo(next)) {
            error("fallback lease return tier $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"fallback lease return tier"} $code"

    fun adjustedExposure(): Double = round(capacity.toDouble() * score * 100) / 100

    fun snapshot(): FallbackLeaseReturnTier {
        return FallbackLeaseReturnTier(
            code = code,
            name = name,
            maximum = maximum,
            priority = priority,
            vehicleId = vehicleId,
            routeCode = routeCode,
            score = score,
            unit = unit,
            capacity = capacity,
            channel = channel,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a fallback lease return tier in status planned. */
fun newFallbackLeaseReturnTier(code: String, name: String): FallbackLeaseReturnTier {
    return FallbackLeaseReturnTier(
        code = code,
        name = name,
        maximum = 0.0,
        priority = 0,
        vehicleId = "",
        routeCode = "",
        score = 0.0,
        unit = "",
        capacity = 0,
        channel = "",
        segment = "",
        status = FallbackLeaseReturnTierStatus.PLANNED,
    )
}
