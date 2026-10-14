package com.acme.transport.customs.hold.holdallocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled hold allocation records of customs's hold work, and the rules, stores, and calculations
// around them.

/** Where a scheduled hold allocation is in its life cycle. */
enum class ScheduledHoldAllocationStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A scheduled hold allocation as the hold team keeps it. */
data class ScheduledHoldAllocation(
    var code: String,
    var label: String,
    var quantity: Int,
    var batchSize: Int,
    var updatedAt: Int,
    var durationMinutes: Int,
    var sequence: Int,
    var reviewer: String,
    var unit: String,
    var status: ScheduledHoldAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledHoldAllocationStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledHoldAllocationStatus): Boolean {
        when (status) {
            ScheduledHoldAllocationStatus.OPEN -> {
                when (next) {
                    ScheduledHoldAllocationStatus.IN_REVIEW -> return true
                }
                return false
            }
            ScheduledHoldAllocationStatus.IN_REVIEW -> {
                when (next) {
                    ScheduledHoldAllocationStatus.REOPENED -> return true
                }
                return false
            }
            ScheduledHoldAllocationStatus.RESOLVED -> {
                when (next) {
                    ScheduledHoldAllocationStatus.REOPENED, ScheduledHoldAllocationStatus.ARCHIVED -> return true
                }
                return false
            }
            ScheduledHoldAllocationStatus.REOPENED -> {
                when (next) {
                    ScheduledHoldAllocationStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledHoldAllocationStatus) {
        if (!canMoveTo(next)) {
            error("scheduled hold allocation $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun adjustedLoad(): Double = round(quantity.toDouble() * durationMinutes.toDouble() * 100) / 100

    fun snapshot(): ScheduledHoldAllocation {
        return ScheduledHoldAllocation(
            code = code,
            label = label,
            quantity = quantity,
            batchSize = batchSize,
            updatedAt = updatedAt,
            durationMinutes = durationMinutes,
            sequence = sequence,
            reviewer = reviewer,
            unit = unit,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a scheduled hold allocation in status open. */
fun newScheduledHoldAllocation(code: String, label: String): ScheduledHoldAllocation {
    return ScheduledHoldAllocation(
        code = code,
        label = label,
        quantity = 0,
        batchSize = 0,
        updatedAt = 0,
        durationMinutes = 0,
        sequence = 0,
        reviewer = "",
        unit = "",
        status = ScheduledHoldAllocationStatus.OPEN,
    )
}
