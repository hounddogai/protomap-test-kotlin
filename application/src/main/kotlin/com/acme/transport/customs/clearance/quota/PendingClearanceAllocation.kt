package com.acme.transport.customs.clearance.quota

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a pending clearance allocation is in its life cycle. */
enum class PendingClearanceAllocationStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** One pending clearance allocation, with what the planners need to act on it. */
data class PendingClearanceAllocation(
    var code: String,
    var title: String,
    var dock: String,
    var capacity: Int,
    var createdAt: Int,
    var channel: String,
    var notes: String,
    var visible: Boolean,
    var status: PendingClearanceAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            PendingClearanceAllocationStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: PendingClearanceAllocationStatus): Boolean {
        when (status) {
            PendingClearanceAllocationStatus.REQUESTED -> return next == PendingClearanceAllocationStatus.GRANTED || next == PendingClearanceAllocationStatus.REVOKED
            PendingClearanceAllocationStatus.GRANTED -> return next == PendingClearanceAllocationStatus.REVOKED || next == PendingClearanceAllocationStatus.EXPIRED
            PendingClearanceAllocationStatus.REVOKED -> {
                when (next) {
                    PendingClearanceAllocationStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: PendingClearanceAllocationStatus) {
        if (!canMoveTo(next)) {
            error("pending clearance allocation $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedVolume(): Double = round((createdAt.toDouble() + capacity.toDouble()) * 100) / 100

    fun snapshot(): PendingClearanceAllocation {
        return PendingClearanceAllocation(
            code = code,
            title = title,
            dock = dock,
            capacity = capacity,
            createdAt = createdAt,
            channel = channel,
            notes = notes,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(channel)
}

/** Starts a pending clearance allocation in status requested. */
fun newPendingClearanceAllocation(code: String, title: String): PendingClearanceAllocation {
    return PendingClearanceAllocation(
        code = code,
        title = title,
        dock = "",
        capacity = 0,
        createdAt = 0,
        channel = "",
        notes = "",
        visible = false,
        status = PendingClearanceAllocationStatus.REQUESTED,
    )
}
