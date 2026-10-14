package com.acme.transport.linehaul.capacity.metric

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal capacity metric is in its life cycle. */
enum class SeasonalCapacityMetricStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** The record of a seasonal capacity metric and its current status. */
data class SeasonalCapacityMetric(
    var code: String,
    var title: String,
    var checksum: String,
    var source: String,
    var currency: String,
    var odometer: Int,
    var windowDays: Int,
    var score: Double,
    var graceDays: Int,
    var carrier: String,
    var status: SeasonalCapacityMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalCapacityMetricStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalCapacityMetricStatus): Boolean {
        when (status) {
            SeasonalCapacityMetricStatus.PROPOSED -> return next == SeasonalCapacityMetricStatus.REJECTED || next == SeasonalCapacityMetricStatus.WITHDRAWN
            SeasonalCapacityMetricStatus.APPROVED -> {
                when (next) {
                    SeasonalCapacityMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
            SeasonalCapacityMetricStatus.REJECTED -> {
                when (next) {
                    SeasonalCapacityMetricStatus.WITHDRAWN -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalCapacityMetricStatus) {
        if (!canMoveTo(next)) {
            error("seasonal capacity metric $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code [$title]"

    fun netVolume(): Double = round((score - windowDays.toDouble()) * 100) / 100

    fun snapshot(): SeasonalCapacityMetric {
        return SeasonalCapacityMetric(
            code = code,
            title = title,
            checksum = checksum,
            source = source,
            currency = currency,
            odometer = odometer,
            windowDays = windowDays,
            score = score,
            graceDays = graceDays,
            carrier = carrier,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a seasonal capacity metric in status proposed. */
fun newSeasonalCapacityMetric(code: String, title: String): SeasonalCapacityMetric {
    return SeasonalCapacityMetric(
        code = code,
        title = title,
        checksum = "",
        source = "",
        currency = "",
        odometer = 0,
        windowDays = 0,
        score = 0.0,
        graceDays = 0,
        carrier = "",
        status = SeasonalCapacityMetricStatus.PROPOSED,
    )
}
