package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scheduled locker allocation is in its life cycle. */
enum class ScheduledLockerAllocationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled locker allocation as the locker team keeps it. */
data class ScheduledLockerAllocation(
    var code: String,
    var label: String,
    var visible: Boolean,
    var unit: String,
    var depot: String,
    var category: String,
    var budget: Double,
    var sequence: Int,
    var active: Boolean,
    var archived: Boolean,
    var status: ScheduledLockerAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledLockerAllocationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledLockerAllocationStatus): Boolean {
        when (status) {
            ScheduledLockerAllocationStatus.OPEN -> {
                when (next) {
                    ScheduledLockerAllocationStatus.IN_REVIEW, ScheduledLockerAllocationStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledLockerAllocationStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledLockerAllocationStatus.RESOLVED -> return true
                }
                return false
            }
            ScheduledLockerAllocationStatus.RESOLVED -> return next == ScheduledLockerAllocationStatus.ARCHIVED
            ScheduledLockerAllocationStatus.REOPENED -> return next == ScheduledLockerAllocationStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: ScheduledLockerAllocationStatus) {
        if (!canMoveTo(next)) {
            error("scheduled locker allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveLoad(): Double = round((budget - sequence.toDouble()) * 100) / 100

    fun snapshot(): ScheduledLockerAllocation {
        return ScheduledLockerAllocation(
            code = code,
            label = label,
            visible = visible,
            unit = unit,
            depot = depot,
            category = category,
            budget = budget,
            sequence = sequence,
            active = active,
            archived = archived,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled locker allocation in status open. */
fun newScheduledLockerAllocation(code: String, label: String): ScheduledLockerAllocation {
    return ScheduledLockerAllocation(
        code = code,
        label = label,
        visible = false,
        unit = "",
        depot = "",
        category = "",
        budget = 0.0,
        sequence = 0,
        active = false,
        archived = false,
        status = ScheduledLockerAllocationStatus.OPEN,
    )
}
