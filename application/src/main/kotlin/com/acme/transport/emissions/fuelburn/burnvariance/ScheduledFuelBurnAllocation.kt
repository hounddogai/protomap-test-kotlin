package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.kit.text.normalize

/** Where a scheduled fuel burn allocation is in its life cycle. */
enum class ScheduledFuelBurnAllocationStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A scheduled fuel burn allocation. Its code is unique within a tenant. */
data class ScheduledFuelBurnAllocation(
    var code: String,
    var label: String,
    var tags: MutableList<String>,
    var contactEmail: String,
    var notes: String,
    var externalRef: String,
    var weight: Double,
    var status: ScheduledFuelBurnAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledFuelBurnAllocationStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledFuelBurnAllocationStatus): Boolean {
        when (status) {
            ScheduledFuelBurnAllocationStatus.CREATED -> {
                when (next) {
                    ScheduledFuelBurnAllocationStatus.VALIDATED -> return true
                }
                return false
            }
            ScheduledFuelBurnAllocationStatus.VALIDATED -> return next == ScheduledFuelBurnAllocationStatus.PUBLISHED || next == ScheduledFuelBurnAllocationStatus.RETIRED
            ScheduledFuelBurnAllocationStatus.PUBLISHED -> return next == ScheduledFuelBurnAllocationStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: ScheduledFuelBurnAllocationStatus) {
        if (!canMoveTo(next)) {
            error("scheduled fuel burn allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun measuredValue(): Double = weight

    fun snapshot(): ScheduledFuelBurnAllocation {
        return ScheduledFuelBurnAllocation(
            code = code,
            label = label,
            tags = tags,
            contactEmail = contactEmail,
            notes = notes,
            externalRef = externalRef,
            weight = weight,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a scheduled fuel burn allocation in status created. */
fun newScheduledFuelBurnAllocation(code: String, label: String): ScheduledFuelBurnAllocation {
    return ScheduledFuelBurnAllocation(
        code = code,
        label = label,
        tags = mutableListOf<String>(),
        contactEmail = "",
        notes = "",
        externalRef = "",
        weight = 0.0,
        status = ScheduledFuelBurnAllocationStatus.CREATED,
    )
}
