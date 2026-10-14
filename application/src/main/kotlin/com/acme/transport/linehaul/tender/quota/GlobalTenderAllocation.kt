package com.acme.transport.linehaul.tender.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a global tender allocation is in its life cycle. */
enum class GlobalTenderAllocationStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One global tender allocation, with what the planners need to act on it. */
data class GlobalTenderAllocation(
    var code: String,
    var title: String,
    var notes: String,
    var retries: Int,
    var revision: Int,
    var depot: String,
    var status: GlobalTenderAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            GlobalTenderAllocationStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: GlobalTenderAllocationStatus): Boolean {
        when (status) {
            GlobalTenderAllocationStatus.IDLE -> return next == GlobalTenderAllocationStatus.WARMING
            GlobalTenderAllocationStatus.WARMING -> {
                when (next) {
                    GlobalTenderAllocationStatus.READY, GlobalTenderAllocationStatus.DRAINING -> return true
                }
                return false
            }
            GlobalTenderAllocationStatus.READY -> return next == GlobalTenderAllocationStatus.DRAINING
            GlobalTenderAllocationStatus.DRAINING -> {
                when (next) {
                    GlobalTenderAllocationStatus.STOPPED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: GlobalTenderAllocationStatus) {
        if (!canMoveTo(next)) {
            error("global tender allocation $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code [$title]"

    fun effectiveExposure(): Double = round((revision.toDouble() + retries.toDouble()) * 100) / 100

    fun snapshot(): GlobalTenderAllocation {
        return GlobalTenderAllocation(
            code = code,
            title = title,
            notes = notes,
            retries = retries,
            revision = revision,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a global tender allocation in status idle. */
fun newGlobalTenderAllocation(code: String, title: String): GlobalTenderAllocation {
    return GlobalTenderAllocation(
        code = code,
        title = title,
        notes = "",
        retries = 0,
        revision = 0,
        depot = "",
        status = GlobalTenderAllocationStatus.IDLE,
    )
}
