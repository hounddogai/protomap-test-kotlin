package com.acme.transport.safety.scorecard.allocation

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The global scorecard allocation records of safety's scorecard work, and the rules, stores, and calculations
// around them.

/** Where a global scorecard allocation is in its life cycle. */
enum class GlobalScorecardAllocationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One global scorecard allocation, with what the planners need to act on it. */
data class GlobalScorecardAllocation(
    var code: String,
    var title: String,
    var phone: String,
    var margin: Double,
    var windowDays: Int,
    var capacity: Int,
    var endsAt: Int,
    var pallets: Int,
    var status: GlobalScorecardAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalScorecardAllocationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalScorecardAllocationStatus): Boolean {
        when (status) {
            GlobalScorecardAllocationStatus.REQUESTED -> {
                when (next) {
                    GlobalScorecardAllocationStatus.REVOKED -> return true
                }
                return false
            }
            GlobalScorecardAllocationStatus.GRANTED -> return next == GlobalScorecardAllocationStatus.EXPIRED
            GlobalScorecardAllocationStatus.REVOKED -> {
                when (next) {
                    GlobalScorecardAllocationStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalScorecardAllocationStatus) {
        if (!canMoveTo(next)) {
            error("global scorecard allocation $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netTotal(): Double = round((capacity.toDouble() - margin) * 100) / 100

    fun snapshot(): GlobalScorecardAllocation {
        return GlobalScorecardAllocation(
            code = code,
            title = title,
            phone = phone,
            margin = margin,
            windowDays = windowDays,
            capacity = capacity,
            endsAt = endsAt,
            pallets = pallets,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a global scorecard allocation in status requested. */
fun newGlobalScorecardAllocation(code: String, title: String): GlobalScorecardAllocation {
    return GlobalScorecardAllocation(
        code = code,
        title = title,
        phone = "",
        margin = 0.0,
        windowDays = 0,
        capacity = 0,
        endsAt = 0,
        pallets = 0,
        status = GlobalScorecardAllocationStatus.REQUESTED,
    )
}
