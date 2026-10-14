package com.acme.transport.customs.hold.group

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling hold allocation is in its life cycle. */
enum class RollingHoldAllocationStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a rolling hold allocation and its current status. */
data class RollingHoldAllocation(
    var code: String,
    var title: String,
    var threshold: Double,
    var revision: Int,
    var driverId: String,
    var tags: MutableList<String>,
    var shiftCode: String,
    var retries: Int,
    var status: RollingHoldAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingHoldAllocationStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingHoldAllocationStatus): Boolean {
        when (status) {
            RollingHoldAllocationStatus.PROPOSED -> {
                when (next) {
                    RollingHoldAllocationStatus.REJECTED, RollingHoldAllocationStatus.WITHDRAWN -> return true
                }
                return false
            }
            RollingHoldAllocationStatus.APPROVED -> return next == RollingHoldAllocationStatus.REJECTED || next == RollingHoldAllocationStatus.WITHDRAWN
            RollingHoldAllocationStatus.REJECTED -> return next == RollingHoldAllocationStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: RollingHoldAllocationStatus) {
        if (!canMoveTo(next)) {
            error("rolling hold allocation $code cannot move from $status to $next")
        }
        status = next
        revision += 1
    }

    fun label(): String = "${"rolling hold allocation"} $code"

    fun hasTag(tag: String): Boolean {
        for (candidate in tags) {
            if (normalize(candidate) == normalize(tag)) {
                return true
            }
        }
        return false
    }

    fun netLoad(): Double = round((retries.toDouble() + revision.toDouble()) * 100) / 100

    fun snapshot(): RollingHoldAllocation {
        return RollingHoldAllocation(
            code = code,
            title = title,
            threshold = threshold,
            revision = revision,
            driverId = driverId,
            tags = tags,
            shiftCode = shiftCode,
            retries = retries,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a rolling hold allocation in status proposed. */
fun newRollingHoldAllocation(code: String, title: String): RollingHoldAllocation {
    return RollingHoldAllocation(
        code = code,
        title = title,
        threshold = 0.0,
        revision = 0,
        driverId = "",
        tags = mutableListOf<String>(),
        shiftCode = "",
        retries = 0,
        status = RollingHoldAllocationStatus.PROPOSED,
    )
}
