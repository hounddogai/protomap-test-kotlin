package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched capacity schedule is in its life cycle. */
enum class BatchedCapacityScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** The record of a batched capacity schedule and its current status. */
data class BatchedCapacitySchedule(
    var code: String,
    var title: String,
    var minimum: Double,
    var rate: Double,
    var lane: String,
    var timezone: String,
    var status: BatchedCapacityScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCapacityScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCapacityScheduleStatus): Boolean {
        when (status) {
            BatchedCapacityScheduleStatus.DRAFT -> {
                when (next) {
                    BatchedCapacityScheduleStatus.PENDING -> return true
                }
                return false
            }
            BatchedCapacityScheduleStatus.PENDING -> return next == BatchedCapacityScheduleStatus.SUSPENDED
            BatchedCapacityScheduleStatus.ACTIVE -> {
                when (next) {
                    BatchedCapacityScheduleStatus.SUSPENDED, BatchedCapacityScheduleStatus.CLOSED -> return true
                }
                return false
            }
            BatchedCapacityScheduleStatus.SUSPENDED -> return next == BatchedCapacityScheduleStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: BatchedCapacityScheduleStatus) {
        if (!canMoveTo(next)) {
            error("batched capacity schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netExposure(): Double = round((rate - minimum) * 100) / 100

    fun snapshot(): BatchedCapacitySchedule {
        return BatchedCapacitySchedule(
            code = code,
            title = title,
            minimum = minimum,
            rate = rate,
            lane = lane,
            timezone = timezone,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a batched capacity schedule in status draft. */
fun newBatchedCapacitySchedule(code: String, title: String): BatchedCapacitySchedule {
    return BatchedCapacitySchedule(
        code = code,
        title = title,
        minimum = 0.0,
        rate = 0.0,
        lane = "",
        timezone = "",
        status = BatchedCapacityScheduleStatus.DRAFT,
    )
}
