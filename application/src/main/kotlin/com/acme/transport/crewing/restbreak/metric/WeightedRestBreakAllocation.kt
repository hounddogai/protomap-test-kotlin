package com.acme.transport.crewing.restbreak.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted rest break allocation is in its life cycle. */
enum class WeightedRestBreakAllocationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One weighted rest break allocation, with what the planners need to act on it. */
data class WeightedRestBreakAllocation(
    var code: String,
    var title: String,
    var distanceKm: Double,
    var routeCode: String,
    var amount: Double,
    var region: String,
    var etaMinutes: Int,
    var status: WeightedRestBreakAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedRestBreakAllocationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedRestBreakAllocationStatus): Boolean {
        when (status) {
            WeightedRestBreakAllocationStatus.REQUESTED -> return next == WeightedRestBreakAllocationStatus.GRANTED || next == WeightedRestBreakAllocationStatus.REVOKED
            WeightedRestBreakAllocationStatus.GRANTED -> return next == WeightedRestBreakAllocationStatus.REVOKED
            WeightedRestBreakAllocationStatus.REVOKED -> {
                when (next) {
                    WeightedRestBreakAllocationStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedRestBreakAllocationStatus) {
        if (!canMoveTo(next)) {
            error("weighted rest break allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted rest break allocation"} $code"

    fun weightedTotal(): Double = round(distanceKm * amount * 100) / 100

    fun snapshot(): WeightedRestBreakAllocation {
        return WeightedRestBreakAllocation(
            code = code,
            title = title,
            distanceKm = distanceKm,
            routeCode = routeCode,
            amount = amount,
            region = region,
            etaMinutes = etaMinutes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(region)
}

/** Starts a weighted rest break allocation in status requested. */
fun newWeightedRestBreakAllocation(code: String, title: String): WeightedRestBreakAllocation {
    return WeightedRestBreakAllocation(
        code = code,
        title = title,
        distanceKm = 0.0,
        routeCode = "",
        amount = 0.0,
        region = "",
        etaMinutes = 0,
        status = WeightedRestBreakAllocationStatus.REQUESTED,
    )
}
