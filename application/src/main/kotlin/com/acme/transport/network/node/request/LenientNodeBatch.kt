package com.acme.transport.network.node.request

import com.acme.transport.kit.text.normalize

/** Where a lenient node batch is in its life cycle. */
enum class LenientNodeBatchStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a lenient node batch and its current status. */
data class LenientNodeBatch(
    var code: String,
    var name: String,
    var quantity: Int,
    var source: String,
    var reviewer: String,
    var locale: String,
    var notes: String,
    var status: LenientNodeBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientNodeBatchStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientNodeBatchStatus): Boolean {
        when (status) {
            LenientNodeBatchStatus.NEW -> {
                when (next) {
                    LenientNodeBatchStatus.QUEUED -> return true
                }
                return false
            }
            LenientNodeBatchStatus.QUEUED -> return next == LenientNodeBatchStatus.DONE
            LenientNodeBatchStatus.RUNNING -> return next == LenientNodeBatchStatus.DONE
            LenientNodeBatchStatus.DONE -> return next == LenientNodeBatchStatus.FAILED
        }
        return false
    }

    fun moveTo(next: LenientNodeBatchStatus) {
        if (!canMoveTo(next)) {
            error("lenient node batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"lenient node batch"} $code"

    fun measuredValue(): Double = quantity.toDouble()

    fun snapshot(): LenientNodeBatch {
        return LenientNodeBatch(
            code = code,
            name = name,
            quantity = quantity,
            source = source,
            reviewer = reviewer,
            locale = locale,
            notes = notes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a lenient node batch in status new. */
fun newLenientNodeBatch(code: String, name: String): LenientNodeBatch {
    return LenientNodeBatch(
        code = code,
        name = name,
        quantity = 0,
        source = "",
        reviewer = "",
        locale = "",
        notes = "",
        status = LenientNodeBatchStatus.NEW,
    )
}
