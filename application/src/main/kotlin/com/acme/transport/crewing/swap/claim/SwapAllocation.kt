package com.acme.transport.crewing.swap.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a swap allocation is in its life cycle. */
enum class SwapAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One swap allocation, with what the planners need to act on it. */
data class SwapAllocation(
    var code: String,
    var title: String,
    var shiftCode: String,
    var cost: Double,
    var priority: Int,
    var margin: Double,
    var status: SwapAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SwapAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SwapAllocationStatus): Boolean {
        when (status) {
            SwapAllocationStatus.DRAFT -> return next == SwapAllocationStatus.PENDING || next == SwapAllocationStatus.CLOSED
            SwapAllocationStatus.PENDING -> {
                when (next) {
                    SwapAllocationStatus.ACTIVE, SwapAllocationStatus.CLOSED -> return true
                }
                return false
            }
            SwapAllocationStatus.ACTIVE -> return next == SwapAllocationStatus.SUSPENDED || next == SwapAllocationStatus.CLOSED
            SwapAllocationStatus.SUSPENDED -> {
                when (next) {
                    SwapAllocationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SwapAllocationStatus) {
        if (!canMoveTo(next)) {
            error("swap allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"swap allocation"} $code"

    fun effectiveValue(): Double = round((priority.toDouble() - margin) * 100) / 100

    fun snapshot(): SwapAllocation {
        return SwapAllocation(
            code = code,
            title = title,
            shiftCode = shiftCode,
            cost = cost,
            priority = priority,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a swap allocation in status draft. */
fun newSwapAllocation(code: String, title: String): SwapAllocation {
    return SwapAllocation(
        code = code,
        title = title,
        shiftCode = "",
        cost = 0.0,
        priority = 0,
        margin = 0.0,
        status = SwapAllocationStatus.DRAFT,
    )
}
