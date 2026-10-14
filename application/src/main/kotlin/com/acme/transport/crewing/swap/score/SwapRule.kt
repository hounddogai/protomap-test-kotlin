package com.acme.transport.crewing.swap.score

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap rule is in its life cycle. */
enum class SwapRuleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A swap rule as the swap team keeps it. */
data class SwapRule(
    var code: String,
    var title: String,
    var minimum: Double,
    var depot: String,
    var priority: Int,
    var archived: Boolean,
    var stopCount: Int,
    var vehicleId: String,
    var status: SwapRuleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapRuleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapRuleStatus): Boolean {
        when (status) {
            SwapRuleStatus.IDLE -> return next == SwapRuleStatus.READY
            SwapRuleStatus.WARMING -> {
                when (next) {
                    SwapRuleStatus.STOPPED -> return true
                }
                return false
            }
            SwapRuleStatus.READY -> return next == SwapRuleStatus.DRAINING
            SwapRuleStatus.DRAINING -> {
                when (next) {
                    SwapRuleStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapRuleStatus) {
        if (!canMoveTo(next)) {
            error("swap rule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $title"

    fun projectedLoad(): Double = round((stopCount.toDouble() - minimum) * 100) / 100

    fun snapshot(): SwapRule {
        return SwapRule(
            code = code,
            title = title,
            minimum = minimum,
            depot = depot,
            priority = priority,
            archived = archived,
            stopCount = stopCount,
            vehicleId = vehicleId,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a swap rule in status idle. */
fun newSwapRule(code: String, title: String): SwapRule {
    return SwapRule(
        code = code,
        title = title,
        minimum = 0.0,
        depot = "",
        priority = 0,
        archived = false,
        stopCount = 0,
        vehicleId = "",
        status = SwapRuleStatus.IDLE,
    )
}
