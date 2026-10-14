package com.acme.transport.crewing.restbreak.breaksummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched rest break record is in its life cycle. */
enum class BatchedRestBreakRecordStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** One batched rest break record, with what the planners need to act on it. */
data class BatchedRestBreakRecord(
    var code: String,
    var title: String,
    var windowDays: Int,
    var routeCode: String,
    var externalRef: String,
    var updatedAt: Int,
    var status: BatchedRestBreakRecordStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRestBreakRecordStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRestBreakRecordStatus): Boolean {
        when (status) {
            BatchedRestBreakRecordStatus.DRAFT -> {
                when (next) {
                    BatchedRestBreakRecordStatus.PENDING, BatchedRestBreakRecordStatus.CLOSED -> return true
                }
                return false
            }
            BatchedRestBreakRecordStatus.PENDING -> {
                when (next) {
                    BatchedRestBreakRecordStatus.ACTIVE, BatchedRestBreakRecordStatus.SUSPENDED -> return true
                }
                return false
            }
            BatchedRestBreakRecordStatus.ACTIVE -> {
                when (next) {
                    BatchedRestBreakRecordStatus.CLOSED -> return true
                }
                return false
            }
            BatchedRestBreakRecordStatus.SUSPENDED -> return next == BatchedRestBreakRecordStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: BatchedRestBreakRecordStatus) {
        if (!canMoveTo(next)) {
            error("batched rest break record $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun adjustedTotal(): Double = round((updatedAt.toDouble() - windowDays.toDouble()) * 100) / 100

    fun snapshot(): BatchedRestBreakRecord {
        return BatchedRestBreakRecord(
            code = code,
            title = title,
            windowDays = windowDays,
            routeCode = routeCode,
            externalRef = externalRef,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched rest break record in status draft. */
fun newBatchedRestBreakRecord(code: String, title: String): BatchedRestBreakRecord {
    return BatchedRestBreakRecord(
        code = code,
        title = title,
        windowDays = 0,
        routeCode = "",
        externalRef = "",
        updatedAt = 0,
        status = BatchedRestBreakRecordStatus.DRAFT,
    )
}
