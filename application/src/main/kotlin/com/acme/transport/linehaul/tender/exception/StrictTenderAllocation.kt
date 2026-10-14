package com.acme.transport.linehaul.tender.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict tender allocation is in its life cycle. */
enum class StrictTenderAllocationStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One strict tender allocation, with what the planners need to act on it. */
data class StrictTenderAllocation(
    var code: String,
    var name: String,
    var revision: Int,
    var archived: Boolean,
    var ceiling: Double,
    var minimum: Double,
    var threshold: Double,
    var status: StrictTenderAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictTenderAllocationStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictTenderAllocationStatus): Boolean {
        when (status) {
            StrictTenderAllocationStatus.DRAFT -> return next == StrictTenderAllocationStatus.ACTIVE || next == StrictTenderAllocationStatus.CLOSED
            StrictTenderAllocationStatus.PENDING -> return next == StrictTenderAllocationStatus.ACTIVE
            StrictTenderAllocationStatus.ACTIVE -> return next == StrictTenderAllocationStatus.SUSPENDED || next == StrictTenderAllocationStatus.CLOSED
            StrictTenderAllocationStatus.SUSPENDED -> {
                when (next) {
                    StrictTenderAllocationStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictTenderAllocationStatus) {
        if (!canMoveTo(next)) {
            error("strict tender allocation $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "$code: $name"

    fun weightedValue(): Double = round((revision.toDouble() - ceiling) * 100) / 100

    fun snapshot(): StrictTenderAllocation {
        return StrictTenderAllocation(
            code = code,
            name = name,
            revision = revision,
            archived = archived,
            ceiling = ceiling,
            minimum = minimum,
            threshold = threshold,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict tender allocation in status draft. */
fun newStrictTenderAllocation(code: String, name: String): StrictTenderAllocation {
    return StrictTenderAllocation(
        code = code,
        name = name,
        revision = 0,
        archived = false,
        ceiling = 0.0,
        minimum = 0.0,
        threshold = 0.0,
        status = StrictTenderAllocationStatus.DRAFT,
    )
}
