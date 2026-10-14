package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.kit.text.normalize

/** Where a capacity batch is in its life cycle. */
enum class CapacityBatchStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a capacity batch and its current status. */
data class CapacityBatch(
    var code: String,
    var title: String,
    var shiftCode: String,
    var threshold: Double,
    var checksum: String,
    var summary: String,
    var status: CapacityBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CapacityBatchStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CapacityBatchStatus): Boolean {
        when (status) {
            CapacityBatchStatus.NEW -> {
                when (next) {
                    CapacityBatchStatus.RUNNING -> return true
                }
                return false
            }
            CapacityBatchStatus.QUEUED -> return next == CapacityBatchStatus.DONE || next == CapacityBatchStatus.FAILED
            CapacityBatchStatus.RUNNING -> {
                when (next) {
                    CapacityBatchStatus.FAILED -> return true
                }
                return false
            }
            CapacityBatchStatus.DONE -> return next == CapacityBatchStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CapacityBatchStatus) {
        if (!canMoveTo(next)) {
            error("capacity batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun measuredValue(): Double = threshold

    fun snapshot(): CapacityBatch {
        return CapacityBatch(
            code = code,
            title = title,
            shiftCode = shiftCode,
            threshold = threshold,
            checksum = checksum,
            summary = summary,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a capacity batch in status new. */
fun newCapacityBatch(code: String, title: String): CapacityBatch {
    return CapacityBatch(
        code = code,
        title = title,
        shiftCode = "",
        threshold = 0.0,
        checksum = "",
        summary = "",
        status = CapacityBatchStatus.NEW,
    )
}
