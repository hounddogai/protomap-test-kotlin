package com.acme.transport.emissions.emissionfactor.preference

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a emission factor allocation is in its life cycle. */
enum class EmissionFactorAllocationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A emission factor allocation. Its code is unique within a tenant. */
data class EmissionFactorAllocation(
    var code: String,
    var name: String,
    var maximum: Double,
    var graceDays: Int,
    var threshold: Double,
    var lane: String,
    var trailerId: String,
    var expiresAt: Int,
    var weight: Double,
    var status: EmissionFactorAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EmissionFactorAllocationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: EmissionFactorAllocationStatus): Boolean {
        when (status) {
            EmissionFactorAllocationStatus.OPEN -> return next == EmissionFactorAllocationStatus.RESOLVED
            EmissionFactorAllocationStatus.IN_REVIEW -> {
                when (next) {
                    EmissionFactorAllocationStatus.RESOLVED, EmissionFactorAllocationStatus.REOPENED -> return true
                }
                return false
            }
            EmissionFactorAllocationStatus.RESOLVED -> return next == EmissionFactorAllocationStatus.REOPENED || next == EmissionFactorAllocationStatus.ARCHIVED
            EmissionFactorAllocationStatus.REOPENED -> return next == EmissionFactorAllocationStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: EmissionFactorAllocationStatus) {
        if (!canMoveTo(next)) {
            error("emission factor allocation $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"emission factor allocation"} $code"

    fun weightedTotal(): Double = round((weight + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): EmissionFactorAllocation {
        return EmissionFactorAllocation(
            code = code,
            name = name,
            maximum = maximum,
            graceDays = graceDays,
            threshold = threshold,
            lane = lane,
            trailerId = trailerId,
            expiresAt = expiresAt,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a emission factor allocation in status open. */
fun newEmissionFactorAllocation(code: String, name: String): EmissionFactorAllocation {
    return EmissionFactorAllocation(
        code = code,
        name = name,
        maximum = 0.0,
        graceDays = 0,
        threshold = 0.0,
        lane = "",
        trailerId = "",
        expiresAt = 0,
        weight = 0.0,
        status = EmissionFactorAllocationStatus.OPEN,
    )
}
