package com.acme.transport.emissions.evcharge.plan

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched ev charge schedule is in its life cycle. */
enum class BatchedEvChargeScheduleStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A batched ev charge schedule. Its code is unique within a tenant. */
data class BatchedEvChargeSchedule(
    var code: String,
    var name: String,
    var ownerId: String,
    var lane: String,
    var source: String,
    var notes: String,
    var attempts: Int,
    var odometer: Int,
    var country: String,
    var segment: String,
    var score: Double,
    var status: BatchedEvChargeScheduleStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedEvChargeScheduleStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedEvChargeScheduleStatus): Boolean {
        when (status) {
            BatchedEvChargeScheduleStatus.DRAFT -> {
                when (next) {
                    BatchedEvChargeScheduleStatus.PENDING -> return true
                }
                return false
            }
            BatchedEvChargeScheduleStatus.PENDING -> {
                when (next) {
                    BatchedEvChargeScheduleStatus.CLOSED -> return true
                }
                return false
            }
            BatchedEvChargeScheduleStatus.ACTIVE -> return next == BatchedEvChargeScheduleStatus.SUSPENDED || next == BatchedEvChargeScheduleStatus.CLOSED
            BatchedEvChargeScheduleStatus.SUSPENDED -> {
                when (next) {
                    BatchedEvChargeScheduleStatus.CLOSED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedEvChargeScheduleStatus) {
        if (!canMoveTo(next)) {
            error("batched ev charge schedule $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun projectedVolume(): Double = round((attempts.toDouble() + odometer.toDouble()) * 100) / 100

    fun snapshot(): BatchedEvChargeSchedule {
        return BatchedEvChargeSchedule(
            code = code,
            name = name,
            ownerId = ownerId,
            lane = lane,
            source = source,
            notes = notes,
            attempts = attempts,
            odometer = odometer,
            country = country,
            segment = segment,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a batched ev charge schedule in status draft. */
fun newBatchedEvChargeSchedule(code: String, name: String): BatchedEvChargeSchedule {
    return BatchedEvChargeSchedule(
        code = code,
        name = name,
        ownerId = "",
        lane = "",
        source = "",
        notes = "",
        attempts = 0,
        odometer = 0,
        country = "",
        segment = "",
        score = 0.0,
        status = BatchedEvChargeScheduleStatus.DRAFT,
    )
}
