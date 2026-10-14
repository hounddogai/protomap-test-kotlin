package com.acme.transport.lastmile.locker.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched locker plan is in its life cycle. */
enum class BatchedLockerPlanStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A batched locker plan as the locker team keeps it. */
data class BatchedLockerPlan(
    var code: String,
    var label: String,
    var dock: String,
    var trailerId: String,
    var attempts: Int,
    var lane: String,
    var stopCount: Int,
    var expiresAt: Int,
    var shiftCode: String,
    var unit: String,
    var active: Boolean,
    var status: BatchedLockerPlanStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedLockerPlanStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedLockerPlanStatus): Boolean {
        when (status) {
            BatchedLockerPlanStatus.NEW -> {
                when (next) {
                    BatchedLockerPlanStatus.QUEUED, BatchedLockerPlanStatus.RUNNING -> return true
                }
                return false
            }
            BatchedLockerPlanStatus.QUEUED -> return next == BatchedLockerPlanStatus.DONE
            BatchedLockerPlanStatus.RUNNING -> return next == BatchedLockerPlanStatus.DONE
            BatchedLockerPlanStatus.DONE -> {
                when (next) {
                    BatchedLockerPlanStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedLockerPlanStatus) {
        if (!canMoveTo(next)) {
            error("batched locker plan $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun weightedValue(): Double = round((attempts.toDouble() - stopCount.toDouble()) * 100) / 100

    fun snapshot(): BatchedLockerPlan {
        return BatchedLockerPlan(
            code = code,
            label = label,
            dock = dock,
            trailerId = trailerId,
            attempts = attempts,
            lane = lane,
            stopCount = stopCount,
            expiresAt = expiresAt,
            shiftCode = shiftCode,
            unit = unit,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a batched locker plan in status new. */
fun newBatchedLockerPlan(code: String, label: String): BatchedLockerPlan {
    return BatchedLockerPlan(
        code = code,
        label = label,
        dock = "",
        trailerId = "",
        attempts = 0,
        lane = "",
        stopCount = 0,
        expiresAt = 0,
        shiftCode = "",
        unit = "",
        active = false,
        status = BatchedLockerPlanStatus.NEW,
    )
}
