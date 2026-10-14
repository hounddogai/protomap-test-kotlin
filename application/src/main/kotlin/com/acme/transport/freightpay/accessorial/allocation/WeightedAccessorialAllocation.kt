package com.acme.transport.freightpay.accessorial.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted accessorial allocation records of freightpay's accessorial work, and the rules, stores, and
// calculations around them.

/** Where a weighted accessorial allocation is in its life cycle. */
enum class WeightedAccessorialAllocationStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A weighted accessorial allocation as the accessorial team keeps it. */
data class WeightedAccessorialAllocation(
    var code: String,
    var label: String,
    var locale: String,
    var threshold: Double,
    var priority: Int,
    var limit: Int,
    var minimum: Double,
    var name: String,
    var createdAt: Int,
    var status: WeightedAccessorialAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedAccessorialAllocationStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedAccessorialAllocationStatus): Boolean {
        when (status) {
            WeightedAccessorialAllocationStatus.PLANNED -> {
                when (next) {
                    WeightedAccessorialAllocationStatus.CANCELLED -> return true
                }
                return false
            }
            WeightedAccessorialAllocationStatus.SCHEDULED -> return next == WeightedAccessorialAllocationStatus.COMPLETED
            WeightedAccessorialAllocationStatus.IN_PROGRESS -> return next == WeightedAccessorialAllocationStatus.CANCELLED
            WeightedAccessorialAllocationStatus.COMPLETED -> {
                when (next) {
                    WeightedAccessorialAllocationStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedAccessorialAllocationStatus) {
        if (!canMoveTo(next)) {
            error("weighted accessorial allocation $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"weighted accessorial allocation"} $code"

    fun weightedLoad(): Double = round(limit.toDouble() * threshold * 100) / 100

    fun snapshot(): WeightedAccessorialAllocation {
        return WeightedAccessorialAllocation(
            code = code,
            label = label,
            locale = locale,
            threshold = threshold,
            priority = priority,
            limit = limit,
            minimum = minimum,
            name = name,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a weighted accessorial allocation in status planned. */
fun newWeightedAccessorialAllocation(code: String, label: String): WeightedAccessorialAllocation {
    return WeightedAccessorialAllocation(
        code = code,
        label = label,
        locale = "",
        threshold = 0.0,
        priority = 0,
        limit = 0,
        minimum = 0.0,
        name = "",
        createdAt = 0,
        status = WeightedAccessorialAllocationStatus.PLANNED,
    )
}
