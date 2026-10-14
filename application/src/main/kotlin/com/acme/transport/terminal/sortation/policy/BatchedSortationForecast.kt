package com.acme.transport.terminal.sortation.policy

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched sortation forecast is in its life cycle. */
enum class BatchedSortationForecastStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** One batched sortation forecast, with what the planners need to act on it. */
data class BatchedSortationForecast(
    var code: String,
    var name: String,
    var amount: Double,
    var carrier: String,
    var description: String,
    var ceiling: Double,
    var timezone: String,
    var attributes: MutableMap<String, String>,
    var status: BatchedSortationForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedSortationForecastStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedSortationForecastStatus): Boolean {
        when (status) {
            BatchedSortationForecastStatus.OPEN -> {
                when (next) {
                    BatchedSortationForecastStatus.REOPENED -> return true
                }
                return false
            }
            BatchedSortationForecastStatus.IN_REVIEW -> return next == BatchedSortationForecastStatus.ARCHIVED
            BatchedSortationForecastStatus.RESOLVED -> {
                when (next) {
                    BatchedSortationForecastStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedSortationForecastStatus.REOPENED -> {
                when (next) {
                    BatchedSortationForecastStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedSortationForecastStatus) {
        if (!canMoveTo(next)) {
            error("batched sortation forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"batched sortation forecast"} $code"

    fun attribute(key: String, fallback: String): String {
        val found = attributes[key] ?: return fallback
        if (found.trim() == "") {
            return fallback
        }
        return found
    }

    fun netValue(): Double = round(amount * ceiling * 100) / 100

    fun snapshot(): BatchedSortationForecast {
        return BatchedSortationForecast(
            code = code,
            name = name,
            amount = amount,
            carrier = carrier,
            description = description,
            ceiling = ceiling,
            timezone = timezone,
            attributes = attributes,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched sortation forecast in status open. */
fun newBatchedSortationForecast(code: String, name: String): BatchedSortationForecast {
    return BatchedSortationForecast(
        code = code,
        name = name,
        amount = 0.0,
        carrier = "",
        description = "",
        ceiling = 0.0,
        timezone = "",
        attributes = mutableMapOf<String, String>(),
        status = BatchedSortationForecastStatus.OPEN,
    )
}
