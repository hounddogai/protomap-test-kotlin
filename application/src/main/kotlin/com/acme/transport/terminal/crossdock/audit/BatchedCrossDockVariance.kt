package com.acme.transport.terminal.crossdock.audit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched cross dock variance is in its life cycle. */
enum class BatchedCrossDockVarianceStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a batched cross dock variance and its current status. */
data class BatchedCrossDockVariance(
    var code: String,
    var title: String,
    var windowDays: Int,
    var category: String,
    var startsAt: Int,
    var segment: String,
    var status: BatchedCrossDockVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedCrossDockVarianceStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedCrossDockVarianceStatus): Boolean {
        when (status) {
            BatchedCrossDockVarianceStatus.PLANNED -> {
                when (next) {
                    BatchedCrossDockVarianceStatus.SCHEDULED -> return true
                }
                return false
            }
            BatchedCrossDockVarianceStatus.SCHEDULED -> {
                when (next) {
                    BatchedCrossDockVarianceStatus.IN_PROGRESS -> return true
                }
                return false
            }
            BatchedCrossDockVarianceStatus.IN_PROGRESS -> {
                when (next) {
                    BatchedCrossDockVarianceStatus.COMPLETED -> return true
                }
                return false
            }
            BatchedCrossDockVarianceStatus.COMPLETED -> return next == BatchedCrossDockVarianceStatus.CANCELLED
        }
        return false
    }

    fun moveTo(next: BatchedCrossDockVarianceStatus) {
        if (!canMoveTo(next)) {
            error("batched cross dock variance $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code ($title)"

    fun weightedVolume(): Double = round(startsAt.toDouble() * windowDays.toDouble() * 100) / 100

    fun snapshot(): BatchedCrossDockVariance {
        return BatchedCrossDockVariance(
            code = code,
            title = title,
            windowDays = windowDays,
            category = category,
            startsAt = startsAt,
            segment = segment,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a batched cross dock variance in status planned. */
fun newBatchedCrossDockVariance(code: String, title: String): BatchedCrossDockVariance {
    return BatchedCrossDockVariance(
        code = code,
        title = title,
        windowDays = 0,
        category = "",
        startsAt = 0,
        segment = "",
        status = BatchedCrossDockVarianceStatus.PLANNED,
    )
}
