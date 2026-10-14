package com.acme.transport.emissions.fuelburn.window

import com.acme.transport.kit.text.normalize

/** Where a rolling fuel burn allocation is in its life cycle. */
enum class RollingFuelBurnAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a rolling fuel burn allocation and its current status. */
data class RollingFuelBurnAllocation(
    var code: String,
    var name: String,
    var visible: Boolean,
    var version: Int,
    var trailerId: String,
    var currency: String,
    var phone: String,
    var country: String,
    var status: RollingFuelBurnAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingFuelBurnAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingFuelBurnAllocationStatus): Boolean {
        when (status) {
            RollingFuelBurnAllocationStatus.DRAFT -> return next == RollingFuelBurnAllocationStatus.ACTIVE
            RollingFuelBurnAllocationStatus.PENDING -> return next == RollingFuelBurnAllocationStatus.ACTIVE || next == RollingFuelBurnAllocationStatus.SUSPENDED
            RollingFuelBurnAllocationStatus.ACTIVE -> return next == RollingFuelBurnAllocationStatus.SUSPENDED || next == RollingFuelBurnAllocationStatus.CLOSED
            RollingFuelBurnAllocationStatus.SUSPENDED -> return next == RollingFuelBurnAllocationStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: RollingFuelBurnAllocationStatus) {
        if (!canMoveTo(next)) {
            error("rolling fuel burn allocation $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code ($name)"

    fun measuredValue(): Double = version.toDouble()

    fun snapshot(): RollingFuelBurnAllocation {
        return RollingFuelBurnAllocation(
            code = code,
            name = name,
            visible = visible,
            version = version,
            trailerId = trailerId,
            currency = currency,
            phone = phone,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a rolling fuel burn allocation in status draft. */
fun newRollingFuelBurnAllocation(code: String, name: String): RollingFuelBurnAllocation {
    return RollingFuelBurnAllocation(
        code = code,
        name = name,
        visible = false,
        version = 0,
        trailerId = "",
        currency = "",
        phone = "",
        country = "",
        status = RollingFuelBurnAllocationStatus.DRAFT,
    )
}
