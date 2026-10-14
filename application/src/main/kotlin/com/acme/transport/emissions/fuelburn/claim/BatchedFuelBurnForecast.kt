package com.acme.transport.emissions.fuelburn.claim

import com.acme.transport.kit.text.normalize

/** Where a batched fuel burn forecast is in its life cycle. */
enum class BatchedFuelBurnForecastStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A batched fuel burn forecast as the fuel burn team keeps it. */
data class BatchedFuelBurnForecast(
    var code: String,
    var label: String,
    var dock: String,
    var source: String,
    var windowDays: Int,
    var status: BatchedFuelBurnForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            BatchedFuelBurnForecastStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: BatchedFuelBurnForecastStatus): Boolean {
        when (status) {
            BatchedFuelBurnForecastStatus.OPEN -> {
                when (next) {
                    BatchedFuelBurnForecastStatus.RESOLVED -> return true
                }
                return false
            }
            BatchedFuelBurnForecastStatus.IN_REVIEW -> return next == BatchedFuelBurnForecastStatus.RESOLVED
            BatchedFuelBurnForecastStatus.RESOLVED -> {
                when (next) {
                    BatchedFuelBurnForecastStatus.REOPENED, BatchedFuelBurnForecastStatus.ARCHIVED -> return true
                }
                return false
            }
            BatchedFuelBurnForecastStatus.REOPENED -> {
                when (next) {
                    BatchedFuelBurnForecastStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: BatchedFuelBurnForecastStatus) {
        if (!canMoveTo(next)) {
            error("batched fuel burn forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code / $label"

    fun measuredValue(): Double = windowDays.toDouble()

    fun snapshot(): BatchedFuelBurnForecast {
        return BatchedFuelBurnForecast(
            code = code,
            label = label,
            dock = dock,
            source = source,
            windowDays = windowDays,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a batched fuel burn forecast in status open. */
fun newBatchedFuelBurnForecast(code: String, label: String): BatchedFuelBurnForecast {
    return BatchedFuelBurnForecast(
        code = code,
        label = label,
        dock = "",
        source = "",
        windowDays = 0,
        status = BatchedFuelBurnForecastStatus.OPEN,
    )
}
