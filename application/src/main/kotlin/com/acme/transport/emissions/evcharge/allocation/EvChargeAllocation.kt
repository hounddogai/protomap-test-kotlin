package com.acme.transport.emissions.evcharge.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The ev charge allocation records of emissions's ev charge work, and the rules, stores, and calculations
// around them.

/** Where a ev charge allocation is in its life cycle. */
enum class EvChargeAllocationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A ev charge allocation. Its code is unique within a tenant. */
data class EvChargeAllocation(
    var code: String,
    var name: String,
    var timezone: String,
    var region: String,
    var retries: Int,
    var externalRef: String,
    var ceiling: Double,
    var priority: Int,
    var channel: String,
    var routeCode: String,
    var pinned: Boolean,
    var status: EvChargeAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            EvChargeAllocationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: EvChargeAllocationStatus): Boolean {
        when (status) {
            EvChargeAllocationStatus.PROPOSED -> {
                when (next) {
                    EvChargeAllocationStatus.APPROVED -> return true
                }
                return false
            }
            EvChargeAllocationStatus.APPROVED -> return next == EvChargeAllocationStatus.REJECTED || next == EvChargeAllocationStatus.WITHDRAWN
            EvChargeAllocationStatus.REJECTED -> return next == EvChargeAllocationStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: EvChargeAllocationStatus) {
        if (!canMoveTo(next)) {
            error("ev charge allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"ev charge allocation"} $code"

    fun projectedVolume(): Double = round(retries.toDouble() * ceiling * 100) / 100

    fun snapshot(): EvChargeAllocation {
        return EvChargeAllocation(
            code = code,
            name = name,
            timezone = timezone,
            region = region,
            retries = retries,
            externalRef = externalRef,
            ceiling = ceiling,
            priority = priority,
            channel = channel,
            routeCode = routeCode,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a ev charge allocation in status proposed. */
fun newEvChargeAllocation(code: String, name: String): EvChargeAllocation {
    return EvChargeAllocation(
        code = code,
        name = name,
        timezone = "",
        region = "",
        retries = 0,
        externalRef = "",
        ceiling = 0.0,
        priority = 0,
        channel = "",
        routeCode = "",
        pinned = false,
        status = EvChargeAllocationStatus.PROPOSED,
    )
}
