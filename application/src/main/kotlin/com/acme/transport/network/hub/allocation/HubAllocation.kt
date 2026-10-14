package com.acme.transport.network.hub.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The hub allocation records of network's hub work, and the rules, stores, and calculations around them.

/** Where a hub allocation is in its life cycle. */
enum class HubAllocationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a hub allocation and its current status. */
data class HubAllocation(
    var code: String,
    var name: String,
    var region: String,
    var ownerId: String,
    var weight: Double,
    var quantity: Int,
    var priority: Int,
    var retries: Int,
    var maximum: Double,
    var status: HubAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            HubAllocationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: HubAllocationStatus): Boolean {
        when (status) {
            HubAllocationStatus.PROPOSED -> return next == HubAllocationStatus.APPROVED || next == HubAllocationStatus.REJECTED
            HubAllocationStatus.APPROVED -> return next == HubAllocationStatus.REJECTED
            HubAllocationStatus.REJECTED -> {
                when (next) {
                    HubAllocationStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: HubAllocationStatus) {
        if (!canMoveTo(next)) {
            error("hub allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedTotal(): Double = round((priority.toDouble() + maximum) * 100) / 100

    fun snapshot(): HubAllocation {
        return HubAllocation(
            code = code,
            name = name,
            region = region,
            ownerId = ownerId,
            weight = weight,
            quantity = quantity,
            priority = priority,
            retries = retries,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a hub allocation in status proposed. */
fun newHubAllocation(code: String, name: String): HubAllocation {
    return HubAllocation(
        code = code,
        name = name,
        region = "",
        ownerId = "",
        weight = 0.0,
        quantity = 0,
        priority = 0,
        retries = 0,
        maximum = 0.0,
        status = HubAllocationStatus.PROPOSED,
    )
}
