package com.acme.transport.network.transittime.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The transit time allocation records of network's transit time work, and the rules, stores, and calculations
// around them.

/** Where a transit time allocation is in its life cycle. */
enum class TransitTimeAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A transit time allocation as the transit time team keeps it. */
data class TransitTimeAllocation(
    var code: String,
    var name: String,
    var sequence: Int,
    var score: Double,
    var retries: Int,
    var cost: Double,
    var vehicleId: String,
    var limit: Int,
    var margin: Double,
    var timezone: String,
    var threshold: Double,
    var status: TransitTimeAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TransitTimeAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: TransitTimeAllocationStatus): Boolean {
        when (status) {
            TransitTimeAllocationStatus.DRAFT -> return next == TransitTimeAllocationStatus.ACTIVE
            TransitTimeAllocationStatus.PENDING -> return next == TransitTimeAllocationStatus.ACTIVE || next == TransitTimeAllocationStatus.CLOSED
            TransitTimeAllocationStatus.ACTIVE -> return next == TransitTimeAllocationStatus.SUSPENDED || next == TransitTimeAllocationStatus.CLOSED
            TransitTimeAllocationStatus.SUSPENDED -> return next == TransitTimeAllocationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: TransitTimeAllocationStatus) {
        if (!canMoveTo(next)) {
            error("transit time allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netExposure(): Double = round((cost - score) * 100) / 100

    fun snapshot(): TransitTimeAllocation {
        return TransitTimeAllocation(
            code = code,
            name = name,
            sequence = sequence,
            score = score,
            retries = retries,
            cost = cost,
            vehicleId = vehicleId,
            limit = limit,
            margin = margin,
            timezone = timezone,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a transit time allocation in status draft. */
fun newTransitTimeAllocation(code: String, name: String): TransitTimeAllocation {
    return TransitTimeAllocation(
        code = code,
        name = name,
        sequence = 0,
        score = 0.0,
        retries = 0,
        cost = 0.0,
        vehicleId = "",
        limit = 0,
        margin = 0.0,
        timezone = "",
        threshold = 0.0,
        status = TransitTimeAllocationStatus.DRAFT,
    )
}
