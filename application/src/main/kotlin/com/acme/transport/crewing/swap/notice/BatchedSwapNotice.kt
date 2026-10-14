package com.acme.transport.crewing.swap.notice

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched swap notice records of crewing's swap work, and the rules, stores, and calculations around
// them.

/** Where a batched swap notice is in its life cycle. */
enum class BatchedSwapNoticeStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A batched swap notice. Its code is unique within a tenant. */
data class BatchedSwapNotice(
    var code: String,
    var title: String,
    var attempts: Int,
    var minimum: Double,
    var priority: Int,
    var quantity: Int,
    var score: Double,
    var maximum: Double,
    var name: String,
    var status: BatchedSwapNoticeStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedSwapNoticeStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedSwapNoticeStatus): Boolean {
        when (status) {
            BatchedSwapNoticeStatus.PROPOSED -> return next == BatchedSwapNoticeStatus.APPROVED || next == BatchedSwapNoticeStatus.WITHDRAWN
            BatchedSwapNoticeStatus.APPROVED -> return next == BatchedSwapNoticeStatus.REJECTED || next == BatchedSwapNoticeStatus.WITHDRAWN
            BatchedSwapNoticeStatus.REJECTED -> return next == BatchedSwapNoticeStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedSwapNoticeStatus) {
        if (!canMoveTo(next)) {
            error("batched swap notice $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code / $title"

    fun weightedValue(): Double = round((minimum + quantity.toDouble()) * 100) / 100

    fun snapshot(): BatchedSwapNotice {
        return BatchedSwapNotice(
            code = code,
            title = title,
            attempts = attempts,
            minimum = minimum,
            priority = priority,
            quantity = quantity,
            score = score,
            maximum = maximum,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a batched swap notice in status proposed. */
fun newBatchedSwapNotice(code: String, title: String): BatchedSwapNotice {
    return BatchedSwapNotice(
        code = code,
        title = title,
        attempts = 0,
        minimum = 0.0,
        priority = 0,
        quantity = 0,
        score = 0.0,
        maximum = 0.0,
        name = "",
        status = BatchedSwapNoticeStatus.PROPOSED,
    )
}
