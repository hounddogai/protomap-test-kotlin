package com.acme.transport.linehaul.tender.tenderforecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The batched tender forecast records of linehaul's tender work, and the rules, stores, and calculations
// around them.

/** Where a batched tender forecast is in its life cycle. */
enum class BatchedTenderForecastStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** One batched tender forecast, with what the planners need to act on it. */
data class BatchedTenderForecast(
    var code: String,
    var label: String,
    var maximum: Double,
    var currency: String,
    var title: String,
    var revision: Int,
    var startsAt: Int,
    var status: BatchedTenderForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedTenderForecastStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedTenderForecastStatus): Boolean {
        when (status) {
            BatchedTenderForecastStatus.PROPOSED -> {
                when (next) {
                    BatchedTenderForecastStatus.APPROVED -> return true
                }
                return false
            }
            BatchedTenderForecastStatus.APPROVED -> {
                when (next) {
                    BatchedTenderForecastStatus.WITHDRAWN -> return true
                }
                return false
            }
            BatchedTenderForecastStatus.REJECTED -> return next == BatchedTenderForecastStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: BatchedTenderForecastStatus) {
        if (!canMoveTo(next)) {
            error("batched tender forecast $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
        revision += 1
    }

    fun displayLabel(): String = "$code / $label"

    fun netLoad(): Double = round((maximum - revision.toDouble()) * 100) / 100

    fun snapshot(): BatchedTenderForecast {
        return BatchedTenderForecast(
            code = code,
            label = label,
            maximum = maximum,
            currency = currency,
            title = title,
            revision = revision,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a batched tender forecast in status proposed. */
fun newBatchedTenderForecast(code: String, label: String): BatchedTenderForecast {
    return BatchedTenderForecast(
        code = code,
        label = label,
        maximum = 0.0,
        currency = "",
        title = "",
        revision = 0,
        startsAt = 0,
        status = BatchedTenderForecastStatus.PROPOSED,
    )
}
