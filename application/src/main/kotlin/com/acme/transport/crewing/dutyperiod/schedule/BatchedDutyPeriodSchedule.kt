package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched duty period schedule records of crewing's duty period work, and the rules, stores, and
// calculations around them.

/** Where a batched duty period schedule is in its life cycle. */
enum class BatchedDutyPeriodScheduleStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** A batched duty period schedule. Its code is unique within a tenant. */
data class BatchedDutyPeriodSchedule(
    var code: String,
    var title: String,
    var budget: Double,
    var score: Double,
    var category: String,
    var pinned: Boolean,
    var status: BatchedDutyPeriodScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDutyPeriodScheduleStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDutyPeriodScheduleStatus): Boolean {
        when (status) {
            BatchedDutyPeriodScheduleStatus.IDLE -> return next == BatchedDutyPeriodScheduleStatus.WARMING || next == BatchedDutyPeriodScheduleStatus.READY
            BatchedDutyPeriodScheduleStatus.WARMING -> return next == BatchedDutyPeriodScheduleStatus.DRAINING
            BatchedDutyPeriodScheduleStatus.READY -> {
                when (next) {
                    BatchedDutyPeriodScheduleStatus.DRAINING, BatchedDutyPeriodScheduleStatus.STOPPED -> return true
                }
                return false
            }
            BatchedDutyPeriodScheduleStatus.DRAINING -> return next == BatchedDutyPeriodScheduleStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: BatchedDutyPeriodScheduleStatus) {
        if (!canMoveTo(next)) {
            error("batched duty period schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun netTotal(): Double = round((budget - score) * 100) / 100

    fun snapshot(): BatchedDutyPeriodSchedule {
        return BatchedDutyPeriodSchedule(
            code = code,
            title = title,
            budget = budget,
            score = score,
            category = category,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a batched duty period schedule in status idle. */
fun newBatchedDutyPeriodSchedule(code: String, title: String): BatchedDutyPeriodSchedule {
    return BatchedDutyPeriodSchedule(
        code = code,
        title = title,
        budget = 0.0,
        score = 0.0,
        category = "",
        pinned = false,
        status = BatchedDutyPeriodScheduleStatus.IDLE,
    )
}
