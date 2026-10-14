package com.acme.transport.emissions.fuelburn.allocation

import com.acme.transport.kit.text.normalize

// The regional fuel burn allocation records of emissions's fuel burn work, and the rules, stores, and
// calculations around them.

/** Where a regional fuel burn allocation is in its life cycle. */
enum class RegionalFuelBurnAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A regional fuel burn allocation. Its code is unique within a tenant. */
data class RegionalFuelBurnAllocation(
    var code: String,
    var label: String,
    var category: String,
    var carrier: String,
    var active: Boolean,
    var archived: Boolean,
    var approved: Boolean,
    var amount: Double,
    var status: RegionalFuelBurnAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalFuelBurnAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalFuelBurnAllocationStatus): Boolean {
        when (status) {
            RegionalFuelBurnAllocationStatus.DRAFT -> {
                when (next) {
                    RegionalFuelBurnAllocationStatus.PENDING, RegionalFuelBurnAllocationStatus.SUSPENDED -> return true
                }
                return false
            }
            RegionalFuelBurnAllocationStatus.PENDING -> return next == RegionalFuelBurnAllocationStatus.SUSPENDED
            RegionalFuelBurnAllocationStatus.ACTIVE -> return next == RegionalFuelBurnAllocationStatus.SUSPENDED || next == RegionalFuelBurnAllocationStatus.CLOSED
            RegionalFuelBurnAllocationStatus.SUSPENDED -> {
                when (next) {
                    RegionalFuelBurnAllocationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalFuelBurnAllocationStatus) {
        if (!canMoveTo(next)) {
            error("regional fuel burn allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "${"regional fuel burn allocation"} $code"

    fun measuredValue(): Double = amount

    fun snapshot(): RegionalFuelBurnAllocation {
        return RegionalFuelBurnAllocation(
            code = code,
            label = label,
            category = category,
            carrier = carrier,
            active = active,
            archived = archived,
            approved = approved,
            amount = amount,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a regional fuel burn allocation in status draft. */
fun newRegionalFuelBurnAllocation(code: String, label: String): RegionalFuelBurnAllocation {
    return RegionalFuelBurnAllocation(
        code = code,
        label = label,
        category = "",
        carrier = "",
        active = false,
        archived = false,
        approved = false,
        amount = 0.0,
        status = RegionalFuelBurnAllocationStatus.DRAFT,
    )
}
