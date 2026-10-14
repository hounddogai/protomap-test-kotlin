package com.acme.transport.crewing.restbreak.breaksummary

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched rest break summary records of crewing's rest break work, and the rules, stores, and
// calculations around them.

/** Where a batched rest break summary is in its life cycle. */
enum class BatchedRestBreakSummaryStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched rest break summary. Its code is unique within a tenant. */
data class BatchedRestBreakSummary(
    var code: String,
    var name: String,
    var discount: Double,
    var contactEmail: String,
    var priority: Int,
    var status: BatchedRestBreakSummaryStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedRestBreakSummaryStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedRestBreakSummaryStatus): Boolean {
        when (status) {
            BatchedRestBreakSummaryStatus.PROPOSED -> {
                when (next) {
                    BatchedRestBreakSummaryStatus.APPROVED, BatchedRestBreakSummaryStatus.REJECTED -> return true
                }
                return false
            }
            BatchedRestBreakSummaryStatus.APPROVED -> return next == BatchedRestBreakSummaryStatus.REJECTED
            BatchedRestBreakSummaryStatus.REJECTED -> {
                when (next) {
                    BatchedRestBreakSummaryStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedRestBreakSummaryStatus) {
        if (!canMoveTo(next)) {
            error("batched rest break summary $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$name]"

    fun adjustedLoad(): Double = round((discount - priority.toDouble()) * 100) / 100

    fun snapshot(): BatchedRestBreakSummary {
        return BatchedRestBreakSummary(
            code = code,
            name = name,
            discount = discount,
            contactEmail = contactEmail,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched rest break summary in status proposed. */
fun newBatchedRestBreakSummary(code: String, name: String): BatchedRestBreakSummary {
    return BatchedRestBreakSummary(
        code = code,
        name = name,
        discount = 0.0,
        contactEmail = "",
        priority = 0,
        status = BatchedRestBreakSummaryStatus.PROPOSED,
    )
}
