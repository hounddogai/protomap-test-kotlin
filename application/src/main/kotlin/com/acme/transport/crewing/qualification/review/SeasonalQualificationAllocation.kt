package com.acme.transport.crewing.qualification.review

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal qualification allocation is in its life cycle. */
enum class SeasonalQualificationAllocationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One seasonal qualification allocation, with what the planners need to act on it. */
data class SeasonalQualificationAllocation(
    var code: String,
    var name: String,
    var graceDays: Int,
    var stopCount: Int,
    var ceiling: Double,
    var priority: Int,
    var depot: String,
    var routeCode: String,
    var trailerId: String,
    var shiftCode: String,
    var status: SeasonalQualificationAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalQualificationAllocationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalQualificationAllocationStatus): Boolean {
        when (status) {
            SeasonalQualificationAllocationStatus.IDLE -> return next == SeasonalQualificationAllocationStatus.WARMING || next == SeasonalQualificationAllocationStatus.STOPPED
            SeasonalQualificationAllocationStatus.WARMING -> return next == SeasonalQualificationAllocationStatus.READY
            SeasonalQualificationAllocationStatus.READY -> {
                when (next) {
                    SeasonalQualificationAllocationStatus.DRAINING, SeasonalQualificationAllocationStatus.STOPPED -> return true
                }
                return false
            }
            SeasonalQualificationAllocationStatus.DRAINING -> return next == SeasonalQualificationAllocationStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: SeasonalQualificationAllocationStatus) {
        if (!canMoveTo(next)) {
            error("seasonal qualification allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"seasonal qualification allocation"} $code"

    fun netLoad(): Double = round((priority.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): SeasonalQualificationAllocation {
        return SeasonalQualificationAllocation(
            code = code,
            name = name,
            graceDays = graceDays,
            stopCount = stopCount,
            ceiling = ceiling,
            priority = priority,
            depot = depot,
            routeCode = routeCode,
            trailerId = trailerId,
            shiftCode = shiftCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a seasonal qualification allocation in status idle. */
fun newSeasonalQualificationAllocation(code: String, name: String): SeasonalQualificationAllocation {
    return SeasonalQualificationAllocation(
        code = code,
        name = name,
        graceDays = 0,
        stopCount = 0,
        ceiling = 0.0,
        priority = 0,
        depot = "",
        routeCode = "",
        trailerId = "",
        shiftCode = "",
        status = SeasonalQualificationAllocationStatus.IDLE,
    )
}
