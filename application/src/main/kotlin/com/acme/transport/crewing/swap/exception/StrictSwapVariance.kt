package com.acme.transport.crewing.swap.exception

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a strict swap variance is in its life cycle. */
enum class StrictSwapVarianceStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** One strict swap variance, with what the planners need to act on it. */
data class StrictSwapVariance(
    var code: String,
    var title: String,
    var maximum: Double,
    var graceDays: Int,
    var externalRef: String,
    var unit: String,
    var margin: Double,
    var minimum: Double,
    var durationMinutes: Int,
    var quantity: Int,
    var startsAt: Int,
    var status: StrictSwapVarianceStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            StrictSwapVarianceStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: StrictSwapVarianceStatus): Boolean {
        when (status) {
            StrictSwapVarianceStatus.NEW -> return next == StrictSwapVarianceStatus.DONE || next == StrictSwapVarianceStatus.FAILED
            StrictSwapVarianceStatus.QUEUED -> return next == StrictSwapVarianceStatus.DONE || next == StrictSwapVarianceStatus.FAILED
            StrictSwapVarianceStatus.RUNNING -> {
                when (next) {
                    StrictSwapVarianceStatus.DONE -> return true
                }
                return false
            }
            StrictSwapVarianceStatus.DONE -> {
                when (next) {
                    StrictSwapVarianceStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: StrictSwapVarianceStatus) {
        if (!canMoveTo(next)) {
            error("strict swap variance $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun projectedLoad(): Double = round((quantity.toDouble() - maximum) * 100) / 100

    fun snapshot(): StrictSwapVariance {
        return StrictSwapVariance(
            code = code,
            title = title,
            maximum = maximum,
            graceDays = graceDays,
            externalRef = externalRef,
            unit = unit,
            margin = margin,
            minimum = minimum,
            durationMinutes = durationMinutes,
            quantity = quantity,
            startsAt = startsAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a strict swap variance in status new. */
fun newStrictSwapVariance(code: String, title: String): StrictSwapVariance {
    return StrictSwapVariance(
        code = code,
        title = title,
        maximum = 0.0,
        graceDays = 0,
        externalRef = "",
        unit = "",
        margin = 0.0,
        minimum = 0.0,
        durationMinutes = 0,
        quantity = 0,
        startsAt = 0,
        status = StrictSwapVarianceStatus.NEW,
    )
}
