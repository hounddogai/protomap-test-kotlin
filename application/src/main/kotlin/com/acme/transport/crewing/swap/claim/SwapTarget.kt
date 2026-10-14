package com.acme.transport.crewing.swap.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap target is in its life cycle. */
enum class SwapTargetStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A swap target as the swap team keeps it. */
data class SwapTarget(
    var code: String,
    var name: String,
    var reviewer: String,
    var budget: Double,
    var updatedAt: Int,
    var trailerId: String,
    var status: SwapTargetStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapTargetStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapTargetStatus): Boolean {
        when (status) {
            SwapTargetStatus.PLANNED -> {
                when (next) {
                    SwapTargetStatus.IN_PROGRESS, SwapTargetStatus.CANCELLED -> return true
                }
                return false
            }
            SwapTargetStatus.SCHEDULED -> return next == SwapTargetStatus.IN_PROGRESS
            SwapTargetStatus.IN_PROGRESS -> return next == SwapTargetStatus.COMPLETED || next == SwapTargetStatus.CANCELLED
            SwapTargetStatus.COMPLETED -> return next == SwapTargetStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: SwapTargetStatus) {
        if (!canMoveTo(next)) {
            error("swap target $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"swap target"} $code"

    fun projectedExposure(): Double = round((updatedAt.toDouble() + budget) * 100) / 100

    fun snapshot(): SwapTarget {
        return SwapTarget(
            code = code,
            name = name,
            reviewer = reviewer,
            budget = budget,
            updatedAt = updatedAt,
            trailerId = trailerId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a swap target in status planned. */
fun newSwapTarget(code: String, name: String): SwapTarget {
    return SwapTarget(
        code = code,
        name = name,
        reviewer = "",
        budget = 0.0,
        updatedAt = 0,
        trailerId = "",
        status = SwapTargetStatus.PLANNED,
    )
}
