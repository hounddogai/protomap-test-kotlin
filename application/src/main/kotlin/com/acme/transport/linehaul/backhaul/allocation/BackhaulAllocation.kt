package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The backhaul allocation records of linehaul's backhaul work, and the rules, stores, and calculations around
// them.

/** Where a backhaul allocation is in its life cycle. */
enum class BackhaulAllocationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A backhaul allocation as the backhaul team keeps it. */
data class BackhaulAllocation(
    var code: String,
    var label: String,
    var score: Double,
    var active: Boolean,
    var budget: Double,
    var pallets: Int,
    var description: String,
    var limit: Int,
    var floorValue: Double,
    var durationMinutes: Int,
    var attempts: Int,
    var status: BackhaulAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BackhaulAllocationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BackhaulAllocationStatus): Boolean {
        when (status) {
            BackhaulAllocationStatus.NEW -> {
                when (next) {
                    BackhaulAllocationStatus.QUEUED -> return true
                }
                return false
            }
            BackhaulAllocationStatus.QUEUED -> {
                when (next) {
                    BackhaulAllocationStatus.RUNNING, BackhaulAllocationStatus.FAILED -> return true
                }
                return false
            }
            BackhaulAllocationStatus.RUNNING -> return next == BackhaulAllocationStatus.DONE || next == BackhaulAllocationStatus.FAILED
            BackhaulAllocationStatus.DONE -> return next == BackhaulAllocationStatus.FAILED
        }
        return false
    }

    fun moveTo(next: BackhaulAllocationStatus) {
        if (!canMoveTo(next)) {
            error("backhaul allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $label"

    fun projectedLoad(): Double = round((limit.toDouble() - durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): BackhaulAllocation {
        return BackhaulAllocation(
            code = code,
            label = label,
            score = score,
            active = active,
            budget = budget,
            pallets = pallets,
            description = description,
            limit = limit,
            floorValue = floorValue,
            durationMinutes = durationMinutes,
            attempts = attempts,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a backhaul allocation in status new. */
fun newBackhaulAllocation(code: String, label: String): BackhaulAllocation {
    return BackhaulAllocation(
        code = code,
        label = label,
        score = 0.0,
        active = false,
        budget = 0.0,
        pallets = 0,
        description = "",
        limit = 0,
        floorValue = 0.0,
        durationMinutes = 0,
        attempts = 0,
        status = BackhaulAllocationStatus.NEW,
    )
}
