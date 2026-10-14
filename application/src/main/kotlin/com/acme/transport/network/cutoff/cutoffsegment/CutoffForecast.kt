package com.acme.transport.network.cutoff.cutoffsegment

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a cutoff forecast is in its life cycle. */
enum class CutoffForecastStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A cutoff forecast as the cutoff team keeps it. */
data class CutoffForecast(
    var code: String,
    var title: String,
    var budget: Double,
    var graceDays: Int,
    var trailerId: String,
    var depot: String,
    var status: CutoffForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            CutoffForecastStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: CutoffForecastStatus): Boolean {
        when (status) {
            CutoffForecastStatus.NEW -> return next == CutoffForecastStatus.RUNNING
            CutoffForecastStatus.QUEUED -> {
                when (next) {
                    CutoffForecastStatus.DONE, CutoffForecastStatus.FAILED -> return true
                }
                return false
            }
            CutoffForecastStatus.RUNNING -> {
                when (next) {
                    CutoffForecastStatus.DONE -> return true
                }
                return false
            }
            CutoffForecastStatus.DONE -> return next == CutoffForecastStatus.FAILED
        }
        return false
    }

    fun moveTo(next: CutoffForecastStatus) {
        if (!canMoveTo(next)) {
            error("cutoff forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code ($title)"

    fun adjustedValue(): Double = round(graceDays.toDouble() * budget * 100) / 100

    fun snapshot(): CutoffForecast {
        return CutoffForecast(
            code = code,
            title = title,
            budget = budget,
            graceDays = graceDays,
            trailerId = trailerId,
            depot = depot,
            status = status,
        )
    }

    fun groupKey(): String = normalize(depot)
}

/** Starts a cutoff forecast in status new. */
fun newCutoffForecast(code: String, title: String): CutoffForecast {
    return CutoffForecast(
        code = code,
        title = title,
        budget = 0.0,
        graceDays = 0,
        trailerId = "",
        depot = "",
        status = CutoffForecastStatus.NEW,
    )
}
