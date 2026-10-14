package com.acme.transport.customs.hold.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched hold allocation is in its life cycle. */
enum class BatchedHoldAllocationStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One batched hold allocation, with what the planners need to act on it. */
data class BatchedHoldAllocation(
    var code: String,
    var title: String,
    var amount: Double,
    var source: String,
    var maximum: Double,
    var version: Int,
    var checksum: String,
    var status: BatchedHoldAllocationStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedHoldAllocationStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedHoldAllocationStatus): Boolean {
        when (status) {
            BatchedHoldAllocationStatus.NEW -> return next == BatchedHoldAllocationStatus.RUNNING
            BatchedHoldAllocationStatus.QUEUED -> {
                when (next) {
                    BatchedHoldAllocationStatus.RUNNING -> return true
                }
                return false
            }
            BatchedHoldAllocationStatus.RUNNING -> return next == BatchedHoldAllocationStatus.DONE || next == BatchedHoldAllocationStatus.FAILED
            BatchedHoldAllocationStatus.DONE -> return next == BatchedHoldAllocationStatus.FAILED
        }
        return false
    }

    fun moveTo(next: BatchedHoldAllocationStatus) {
        if (!canMoveTo(next)) {
            error("batched hold allocation $code cannot move from $status to $next")
        }
        status = next
        version += 1
    }

    fun label(): String = "$code / $title"

    fun weightedTotal(): Double = round((maximum - amount) * 100) / 100

    fun snapshot(): BatchedHoldAllocation {
        return BatchedHoldAllocation(
            code = code,
            title = title,
            amount = amount,
            source = source,
            maximum = maximum,
            version = version,
            checksum = checksum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched hold allocation in status new. */
fun newBatchedHoldAllocation(code: String, title: String): BatchedHoldAllocation {
    return BatchedHoldAllocation(
        code = code,
        title = title,
        amount = 0.0,
        source = "",
        maximum = 0.0,
        version = 0,
        checksum = "",
        status = BatchedHoldAllocationStatus.NEW,
    )
}
