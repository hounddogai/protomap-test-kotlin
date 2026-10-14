package com.acme.transport.safety.scorecard.ledger

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a scorecard allocation is in its life cycle. */
enum class ScorecardAllocationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One scorecard allocation, with what the planners need to act on it. */
data class ScorecardAllocation(
    var code: String,
    var title: String,
    var driverId: String,
    var country: String,
    var segment: String,
    var capacity: Int,
    var depot: String,
    var durationMinutes: Int,
    var routeCode: String,
    var status: ScorecardAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScorecardAllocationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: ScorecardAllocationStatus): Boolean {
        when (status) {
            ScorecardAllocationStatus.PROPOSED -> {
                when (next) {
                    ScorecardAllocationStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScorecardAllocationStatus.APPROVED -> {
                when (next) {
                    ScorecardAllocationStatus.REJECTED, ScorecardAllocationStatus.WITHDRAWN -> return true
                }
                return false
            }
            ScorecardAllocationStatus.REJECTED -> return next == ScorecardAllocationStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: ScorecardAllocationStatus) {
        if (!canMoveTo(next)) {
            error("scorecard allocation $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun adjustedVolume(): Double = round((durationMinutes.toDouble() - capacity.toDouble()) * 100) / 100

    fun snapshot(): ScorecardAllocation {
        return ScorecardAllocation(
            code = code,
            title = title,
            driverId = driverId,
            country = country,
            segment = segment,
            capacity = capacity,
            depot = depot,
            durationMinutes = durationMinutes,
            routeCode = routeCode,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a scorecard allocation in status proposed. */
fun newScorecardAllocation(code: String, title: String): ScorecardAllocation {
    return ScorecardAllocation(
        code = code,
        title = title,
        driverId = "",
        country = "",
        segment = "",
        capacity = 0,
        depot = "",
        durationMinutes = 0,
        routeCode = "",
        status = ScorecardAllocationStatus.PROPOSED,
    )
}
