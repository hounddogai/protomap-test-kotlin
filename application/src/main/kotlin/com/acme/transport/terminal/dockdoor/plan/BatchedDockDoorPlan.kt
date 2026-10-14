package com.acme.transport.terminal.dockdoor.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched dock door plan records of terminal's dock door work, and the rules, stores, and calculations
// around them.

/** Where a batched dock door plan is in its life cycle. */
enum class BatchedDockDoorPlanStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** The record of a batched dock door plan and its current status. */
data class BatchedDockDoorPlan(
    var code: String,
    var title: String,
    var quantity: Int,
    var name: String,
    var etaMinutes: Int,
    var cost: Double,
    var updatedAt: Int,
    var retries: Int,
    var windowDays: Int,
    var ceiling: Double,
    var tags: MutableList<String>,
    var status: BatchedDockDoorPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDockDoorPlanStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDockDoorPlanStatus): Boolean {
        when (status) {
            BatchedDockDoorPlanStatus.REQUESTED -> {
                when (next) {
                    BatchedDockDoorPlanStatus.REVOKED -> return true
                }
                return false
            }
            BatchedDockDoorPlanStatus.GRANTED -> return next == BatchedDockDoorPlanStatus.REVOKED || next == BatchedDockDoorPlanStatus.EXPIRED
            BatchedDockDoorPlanStatus.REVOKED -> {
                when (next) {
                    BatchedDockDoorPlanStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedDockDoorPlanStatus) {
        if (!canMoveTo(next)) {
            error("batched dock door plan $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"batched dock door plan"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun projectedVolume(): Double = round(etaMinutes.toDouble() * quantity.toDouble() * 100) / 100

    fun snapshot(): BatchedDockDoorPlan {
        return BatchedDockDoorPlan(
            code = code,
            title = title,
            quantity = quantity,
            name = name,
            etaMinutes = etaMinutes,
            cost = cost,
            updatedAt = updatedAt,
            retries = retries,
            windowDays = windowDays,
            ceiling = ceiling,
            tags = tags,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched dock door plan in status requested. */
fun newBatchedDockDoorPlan(code: String, title: String): BatchedDockDoorPlan {
    return BatchedDockDoorPlan(
        code = code,
        title = title,
        quantity = 0,
        name = "",
        etaMinutes = 0,
        cost = 0.0,
        updatedAt = 0,
        retries = 0,
        windowDays = 0,
        ceiling = 0.0,
        tags = mutableListOf<String>(),
        status = BatchedDockDoorPlanStatus.REQUESTED,
    )
}
