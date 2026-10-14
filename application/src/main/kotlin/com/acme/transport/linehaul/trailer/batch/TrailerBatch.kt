package com.acme.transport.linehaul.trailer.batch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The trailer batch records of linehaul's trailer work, and the rules, stores, and calculations around them.

/** Where a trailer batch is in its life cycle. */
enum class TrailerBatchStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** One trailer batch, with what the planners need to act on it. */
data class TrailerBatch(
    var code: String,
    var label: String,
    var phone: String,
    var createdAt: Int,
    var weight: Double,
    var graceDays: Int,
    var reviewer: String,
    var status: TrailerBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TrailerBatchStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TrailerBatchStatus): Boolean {
        when (status) {
            TrailerBatchStatus.CREATED -> return next == TrailerBatchStatus.VALIDATED
            TrailerBatchStatus.VALIDATED -> return next == TrailerBatchStatus.PUBLISHED || next == TrailerBatchStatus.RETIRED
            TrailerBatchStatus.PUBLISHED -> return next == TrailerBatchStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TrailerBatchStatus) {
        if (!canMoveTo(next)) {
            error("trailer batch $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"trailer batch"} $code"

    fun adjustedExposure(): Double = round((weight + graceDays.toDouble()) * 100) / 100

    fun snapshot(): TrailerBatch {
        return TrailerBatch(
            code = code,
            label = label,
            phone = phone,
            createdAt = createdAt,
            weight = weight,
            graceDays = graceDays,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a trailer batch in status created. */
fun newTrailerBatch(code: String, label: String): TrailerBatch {
    return TrailerBatch(
        code = code,
        label = label,
        phone = "",
        createdAt = 0,
        weight = 0.0,
        graceDays = 0,
        reviewer = "",
        status = TrailerBatchStatus.CREATED,
    )
}
