package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted trailer batch records of linehaul's trailer work, and the rules, stores, and calculations
// around them.

/** Where a weighted trailer batch is in its life cycle. */
enum class WeightedTrailerBatchStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A weighted trailer batch. Its code is unique within a tenant. */
data class WeightedTrailerBatch(
    var code: String,
    var title: String,
    var carrier: String,
    var threshold: Double,
    var updatedAt: Int,
    var status: WeightedTrailerBatchStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedTrailerBatchStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedTrailerBatchStatus): Boolean {
        when (status) {
            WeightedTrailerBatchStatus.DRAFT -> {
                when (next) {
                    WeightedTrailerBatchStatus.ACTIVE, WeightedTrailerBatchStatus.CLOSED -> return true
                }
                return false
            }
            WeightedTrailerBatchStatus.PENDING -> {
                when (next) {
                    WeightedTrailerBatchStatus.SUSPENDED -> return true
                }
                return false
            }
            WeightedTrailerBatchStatus.ACTIVE -> {
                when (next) {
                    WeightedTrailerBatchStatus.CLOSED -> return true
                }
                return false
            }
            WeightedTrailerBatchStatus.SUSPENDED -> return next == WeightedTrailerBatchStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: WeightedTrailerBatchStatus) {
        if (!canMoveTo(next)) {
            error("weighted trailer batch $code cannot move from $status to $next")
        }
        status = next
        updatedAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun netLoad(): Double = round(updatedAt.toDouble() * threshold * 100) / 100

    fun snapshot(): WeightedTrailerBatch {
        return WeightedTrailerBatch(
            code = code,
            title = title,
            carrier = carrier,
            threshold = threshold,
            updatedAt = updatedAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted trailer batch in status draft. */
fun newWeightedTrailerBatch(code: String, title: String): WeightedTrailerBatch {
    return WeightedTrailerBatch(
        code = code,
        title = title,
        carrier = "",
        threshold = 0.0,
        updatedAt = 0,
        status = WeightedTrailerBatchStatus.DRAFT,
    )
}
