package com.acme.transport.lastmile.locker.adjustment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched locker adjustment records of lastmile's locker work, and the rules, stores, and calculations
// around them.

/** Where a batched locker adjustment is in its life cycle. */
enum class BatchedLockerAdjustmentStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One batched locker adjustment, with what the planners need to act on it. */
data class BatchedLockerAdjustment(
    var code: String,
    var name: String,
    var loadKg: Double,
    var endsAt: Int,
    var durationMinutes: Int,
    var category: String,
    var status: BatchedLockerAdjustmentStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedLockerAdjustmentStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedLockerAdjustmentStatus): Boolean {
        when (status) {
            BatchedLockerAdjustmentStatus.DRAFT -> {
                when (next) {
                    BatchedLockerAdjustmentStatus.PENDING, BatchedLockerAdjustmentStatus.ACTIVE -> return true
                }
                return false
            }
            BatchedLockerAdjustmentStatus.PENDING -> {
                when (next) {
                    BatchedLockerAdjustmentStatus.ACTIVE, BatchedLockerAdjustmentStatus.CLOSED -> return true
                }
                return false
            }
            BatchedLockerAdjustmentStatus.ACTIVE -> {
                when (next) {
                    BatchedLockerAdjustmentStatus.SUSPENDED, BatchedLockerAdjustmentStatus.CLOSED -> return true
                }
                return false
            }
            BatchedLockerAdjustmentStatus.SUSPENDED -> return next == BatchedLockerAdjustmentStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: BatchedLockerAdjustmentStatus) {
        if (!canMoveTo(next)) {
            error("batched locker adjustment $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"batched locker adjustment"} $code"

    fun projectedLoad(): Double = round(loadKg * endsAt.toDouble() * 100) / 100

    fun snapshot(): BatchedLockerAdjustment {
        return BatchedLockerAdjustment(
            code = code,
            name = name,
            loadKg = loadKg,
            endsAt = endsAt,
            durationMinutes = durationMinutes,
            category = category,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a batched locker adjustment in status draft. */
fun newBatchedLockerAdjustment(code: String, name: String): BatchedLockerAdjustment {
    return BatchedLockerAdjustment(
        code = code,
        name = name,
        loadKg = 0.0,
        endsAt = 0,
        durationMinutes = 0,
        category = "",
        status = BatchedLockerAdjustmentStatus.DRAFT,
    )
}
