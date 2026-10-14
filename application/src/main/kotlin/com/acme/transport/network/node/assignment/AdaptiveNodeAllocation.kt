package com.acme.transport.network.node.assignment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a adaptive node allocation is in its life cycle. */
enum class AdaptiveNodeAllocationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a adaptive node allocation and its current status. */
data class AdaptiveNodeAllocation(
    var code: String,
    var name: String,
    var category: String,
    var durationMinutes: Int,
    var maximum: Double,
    var status: AdaptiveNodeAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            AdaptiveNodeAllocationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: AdaptiveNodeAllocationStatus): Boolean {
        when (status) {
            AdaptiveNodeAllocationStatus.PROPOSED -> return next == AdaptiveNodeAllocationStatus.WITHDRAWN
            AdaptiveNodeAllocationStatus.APPROVED -> {
                when (next) {
                    AdaptiveNodeAllocationStatus.WITHDRAWN -> return true
                }
                return false
            }
            AdaptiveNodeAllocationStatus.REJECTED -> {
                when (next) {
                    AdaptiveNodeAllocationStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: AdaptiveNodeAllocationStatus) {
        if (!canMoveTo(next)) {
            error("adaptive node allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedLoad(): Double = round((maximum + durationMinutes.toDouble()) * 100) / 100

    fun snapshot(): AdaptiveNodeAllocation {
        return AdaptiveNodeAllocation(
            code = code,
            name = name,
            category = category,
            durationMinutes = durationMinutes,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a adaptive node allocation in status proposed. */
fun newAdaptiveNodeAllocation(code: String, name: String): AdaptiveNodeAllocation {
    return AdaptiveNodeAllocation(
        code = code,
        name = name,
        category = "",
        durationMinutes = 0,
        maximum = 0.0,
        status = AdaptiveNodeAllocationStatus.PROPOSED,
    )
}
