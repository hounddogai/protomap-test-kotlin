package com.acme.transport.terminal.sortation.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The sortation forecast records of terminal's sortation work, and the rules, stores, and calculations around
// them.

/** Where a sortation forecast is in its life cycle. */
enum class SortationForecastStatus {
    DRAFT, PENDING, ACTIVE, SUSPENDED, CLOSED,
}

/** A sortation forecast. Its code is unique within a tenant. */
data class SortationForecast(
    var code: String,
    var label: String,
    var cost: Double,
    var attempts: Int,
    var startsAt: Int,
    var status: SortationForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SortationForecastStatus.CLOSED -> return true
        }
        return false
    }

    fun canMoveTo(next: SortationForecastStatus): Boolean {
        when (status) {
            SortationForecastStatus.DRAFT -> {
                when (next) {
                    SortationForecastStatus.SUSPENDED -> return true
                }
                return false
            }
            SortationForecastStatus.PENDING -> {
                when (next) {
                    SortationForecastStatus.ACTIVE, SortationForecastStatus.CLOSED -> return true
                }
                return false
            }
            SortationForecastStatus.ACTIVE -> return next == SortationForecastStatus.SUSPENDED
            SortationForecastStatus.SUSPENDED -> return next == SortationForecastStatus.CLOSED
        }
        return false
    }

    fun moveTo(next: SortationForecastStatus) {
        if (!canMoveTo(next)) {
            error("sortation forecast $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun projectedExposure(): Double = round((attempts.toDouble() - startsAt.toDouble()) * 100) / 100

    fun snapshot(): SortationForecast {
        return SortationForecast(
            code = code,
            label = label,
            cost = cost,
            attempts = attempts,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a sortation forecast in status draft. */
fun newSortationForecast(code: String, label: String): SortationForecast {
    return SortationForecast(
        code = code,
        label = label,
        cost = 0.0,
        attempts = 0,
        startsAt = 0,
        status = SortationForecastStatus.DRAFT,
    )
}
