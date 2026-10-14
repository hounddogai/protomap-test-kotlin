package com.acme.transport.fleetops.defect.budget

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict defect batch is in its life cycle. */
enum class StrictDefectBatchStatus {
    IDLE, WARMING, READY, DRAINING, STOPPED,
}

/** One strict defect batch, with what the planners need to act on it. */
data class StrictDefectBatch(
    var code: String,
    var name: String,
    var stopCount: Int,
    var discount: Double,
    var floorValue: Double,
    var threshold: Double,
    var active: Boolean,
    var status: StrictDefectBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictDefectBatchStatus.STOPPED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictDefectBatchStatus): Boolean {
        when (status) {
            StrictDefectBatchStatus.IDLE -> return next == StrictDefectBatchStatus.WARMING
            StrictDefectBatchStatus.WARMING -> return next == StrictDefectBatchStatus.READY || next == StrictDefectBatchStatus.STOPPED
            StrictDefectBatchStatus.READY -> {
                when (next) {
                    StrictDefectBatchStatus.DRAINING, StrictDefectBatchStatus.STOPPED -> return true
                }
                return false
            }
            StrictDefectBatchStatus.DRAINING -> return next == StrictDefectBatchStatus.STOPPED
        }
        return false
    }

    fun moveTo(next: StrictDefectBatchStatus) {
        if (!canMoveTo(next)) {
            error("strict defect batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $name"

    fun projectedValue(): Double = round(threshold * floorValue * 100) / 100

    fun snapshot(): StrictDefectBatch {
        return StrictDefectBatch(
            code = code,
            name = name,
            stopCount = stopCount,
            discount = discount,
            floorValue = floorValue,
            threshold = threshold,
            active = active,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a strict defect batch in status idle. */
fun newStrictDefectBatch(code: String, name: String): StrictDefectBatch {
    return StrictDefectBatch(
        code = code,
        name = name,
        stopCount = 0,
        discount = 0.0,
        floorValue = 0.0,
        threshold = 0.0,
        active = false,
        status = StrictDefectBatchStatus.IDLE,
    )
}
