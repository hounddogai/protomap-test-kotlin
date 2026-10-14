package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched hold schedule is in its life cycle. */
enum class BatchedHoldScheduleStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One batched hold schedule, with what the planners need to act on it. */
data class BatchedHoldSchedule(
    var code: String,
    var label: String,
    var checksum: String,
    var revision: Int,
    var endsAt: Int,
    var driverId: String,
    var quantity: Int,
    var capacity: Int,
    var durationMinutes: Int,
    var batchSize: Int,
    var summary: String,
    var status: BatchedHoldScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedHoldScheduleStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedHoldScheduleStatus): Boolean {
        when (status) {
            BatchedHoldScheduleStatus.PROPOSED -> return next == BatchedHoldScheduleStatus.APPROVED || next == BatchedHoldScheduleStatus.REJECTED
            BatchedHoldScheduleStatus.APPROVED -> {
                when (next) {
                    BatchedHoldScheduleStatus.REJECTED -> return true
                }
                return false
            }
            BatchedHoldScheduleStatus.REJECTED -> return next == BatchedHoldScheduleStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedHoldScheduleStatus) {
        if (!canMoveTo(next)) {
            error("batched hold schedule $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun effectiveExposure(): Double = round((capacity.toDouble() - batchSize.toDouble()) * 100) / 100

    fun snapshot(): BatchedHoldSchedule {
        return BatchedHoldSchedule(
            code = code,
            label = label,
            checksum = checksum,
            revision = revision,
            endsAt = endsAt,
            driverId = driverId,
            quantity = quantity,
            capacity = capacity,
            durationMinutes = durationMinutes,
            batchSize = batchSize,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched hold schedule in status proposed. */
fun newBatchedHoldSchedule(code: String, label: String): BatchedHoldSchedule {
    return BatchedHoldSchedule(
        code = code,
        label = label,
        checksum = "",
        revision = 0,
        endsAt = 0,
        driverId = "",
        quantity = 0,
        capacity = 0,
        durationMinutes = 0,
        batchSize = 0,
        summary = "",
        status = BatchedHoldScheduleStatus.PROPOSED,
    )
}
