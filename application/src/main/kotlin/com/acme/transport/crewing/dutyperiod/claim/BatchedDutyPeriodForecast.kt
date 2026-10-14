package com.acme.transport.crewing.dutyperiod.claim

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a batched duty period forecast is in its life cycle. */
enum class BatchedDutyPeriodForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A batched duty period forecast as the duty period team keeps it. */
data class BatchedDutyPeriodForecast(
    var code: String,
    var name: String,
    var attempts: Int,
    var maximum: Double,
    var reviewer: String,
    var status: BatchedDutyPeriodForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedDutyPeriodForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedDutyPeriodForecastStatus): Boolean {
        when (status) {
            BatchedDutyPeriodForecastStatus.CREATED -> return next == BatchedDutyPeriodForecastStatus.RETIRED
            BatchedDutyPeriodForecastStatus.VALIDATED -> return next == BatchedDutyPeriodForecastStatus.PUBLISHED || next == BatchedDutyPeriodForecastStatus.RETIRED
            BatchedDutyPeriodForecastStatus.PUBLISHED -> {
                when (next) {
                    BatchedDutyPeriodForecastStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedDutyPeriodForecastStatus) {
        if (!canMoveTo(next)) {
            error("batched duty period forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($name)"

    fun netLoad(): Double = round((attempts.toDouble() - maximum) * 100) / 100

    fun snapshot(): BatchedDutyPeriodForecast {
        return BatchedDutyPeriodForecast(
            code = code,
            name = name,
            attempts = attempts,
            maximum = maximum,
            reviewer = reviewer,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a batched duty period forecast in status created. */
fun newBatchedDutyPeriodForecast(code: String, name: String): BatchedDutyPeriodForecast {
    return BatchedDutyPeriodForecast(
        code = code,
        name = name,
        attempts = 0,
        maximum = 0.0,
        reviewer = "",
        status = BatchedDutyPeriodForecastStatus.CREATED,
    )
}
