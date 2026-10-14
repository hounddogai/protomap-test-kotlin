package com.acme.transport.crewing.roster.agreement

import com.acme.transport.kit.text.normalize

/** Where a batched roster adjustment is in its life cycle. */
enum class BatchedRosterAdjustmentStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One batched roster adjustment, with what the planners need to act on it. */
data class BatchedRosterAdjustment(
    var code: String,
    var label: String,
    var priority: Int,
    var shiftCode: String,
    var reviewer: String,
    var notes: String,
    var country: String,
    var status: BatchedRosterAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRosterAdjustmentStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRosterAdjustmentStatus): Boolean {
        when (status) {
            BatchedRosterAdjustmentStatus.IDLE -> {
                when (next) {
                    BatchedRosterAdjustmentStatus.STOPPED -> return true
                }
                return false
            }
            BatchedRosterAdjustmentStatus.WARMING -> return next == BatchedRosterAdjustmentStatus.DRAINING || next == BatchedRosterAdjustmentStatus.STOPPED
            BatchedRosterAdjustmentStatus.READY -> return next == BatchedRosterAdjustmentStatus.STOPPED
            BatchedRosterAdjustmentStatus.DRAINING -> return next == BatchedRosterAdjustmentStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BatchedRosterAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("batched roster adjustment $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code [$label]"

    fun measuredValue(): Double = priority.toDouble()

    fun snapshot(): BatchedRosterAdjustment {
        return BatchedRosterAdjustment(
            code = code,
            label = label,
            priority = priority,
            shiftCode = shiftCode,
            reviewer = reviewer,
            notes = notes,
            country = country,
            status = status,
        )
    }

    fun groupKey(): String = normalize(country)
}

/** Starts a batched roster adjustment in status idle. */
fun newBatchedRosterAdjustment(code: String, label: String): BatchedRosterAdjustment {
    return BatchedRosterAdjustment(
        code = code,
        label = label,
        priority = 0,
        shiftCode = "",
        reviewer = "",
        notes = "",
        country = "",
        status = BatchedRosterAdjustmentStatus.IDLE,
    )
}
