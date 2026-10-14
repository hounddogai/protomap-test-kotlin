package com.acme.transport.crewing.restbreak.breakentry

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched rest break threshold is in its life cycle. */
enum class BatchedRestBreakThresholdStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A batched rest break threshold as the rest break team keeps it. */
data class BatchedRestBreakThreshold(
    var code: String,
    var label: String,
    var endsAt: Int,
    var ownerId: String,
    var shiftCode: String,
    var reviewer: String,
    var limit: Int,
    var floorValue: Double,
    var ceiling: Double,
    var capacity: Int,
    var visible: Boolean,
    var status: BatchedRestBreakThresholdStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRestBreakThresholdStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRestBreakThresholdStatus): Boolean {
        when (status) {
            BatchedRestBreakThresholdStatus.NEW -> return next == BatchedRestBreakThresholdStatus.DONE || next == BatchedRestBreakThresholdStatus.FAILED
            BatchedRestBreakThresholdStatus.QUEUED -> return next == BatchedRestBreakThresholdStatus.DONE
            BatchedRestBreakThresholdStatus.RUNNING -> {
                when (next) {
                    BatchedRestBreakThresholdStatus.DONE -> return true
                }
                return false
            }
            BatchedRestBreakThresholdStatus.DONE -> {
                when (next) {
                    BatchedRestBreakThresholdStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedRestBreakThresholdStatus) {
        if (!canMoveTo(next)) {
            error("batched rest break threshold $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun effectiveTotal(): Double = round(floorValue * endsAt.toDouble() * 100) / 100

    fun snapshot(): BatchedRestBreakThreshold {
        return BatchedRestBreakThreshold(
            code = code,
            label = label,
            endsAt = endsAt,
            ownerId = ownerId,
            shiftCode = shiftCode,
            reviewer = reviewer,
            limit = limit,
            floorValue = floorValue,
            ceiling = ceiling,
            capacity = capacity,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched rest break threshold in status new. */
fun newBatchedRestBreakThreshold(code: String, label: String): BatchedRestBreakThreshold {
    return BatchedRestBreakThreshold(
        code = code,
        label = label,
        endsAt = 0,
        ownerId = "",
        shiftCode = "",
        reviewer = "",
        limit = 0,
        floorValue = 0.0,
        ceiling = 0.0,
        capacity = 0,
        visible = false,
        status = BatchedRestBreakThresholdStatus.NEW,
    )
}
