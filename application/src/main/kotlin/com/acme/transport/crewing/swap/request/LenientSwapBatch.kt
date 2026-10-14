package com.acme.transport.crewing.swap.request

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a lenient swap batch is in its life cycle. */
enum class LenientSwapBatchStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One lenient swap batch, with what the planners need to act on it. */
data class LenientSwapBatch(
    var code: String,
    var name: String,
    var minimum: Double,
    var score: Double,
    var budget: Double,
    var status: LenientSwapBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            LenientSwapBatchStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: LenientSwapBatchStatus): Boolean {
        when (status) {
            LenientSwapBatchStatus.PROPOSED -> return next == LenientSwapBatchStatus.APPROVED
            LenientSwapBatchStatus.APPROVED -> return next == LenientSwapBatchStatus.REJECTED || next == LenientSwapBatchStatus.WITHDRAWN
            LenientSwapBatchStatus.REJECTED -> return next == LenientSwapBatchStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: LenientSwapBatchStatus) {
        if (!canMoveTo(next)) {
            error("lenient swap batch $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun effectiveTotal(): Double = round((score + budget) * 100) / 100

    fun snapshot(): LenientSwapBatch {
        return LenientSwapBatch(
            code = code,
            name = name,
            minimum = minimum,
            score = score,
            budget = budget,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a lenient swap batch in status proposed. */
fun newLenientSwapBatch(code: String, name: String): LenientSwapBatch {
    return LenientSwapBatch(
        code = code,
        name = name,
        minimum = 0.0,
        score = 0.0,
        budget = 0.0,
        status = LenientSwapBatchStatus.PROPOSED,
    )
}
