package com.acme.transport.network.transittime.timethreshold

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a seasonal transit time metric is in its life cycle. */
enum class SeasonalTransitTimeMetricStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A seasonal transit time metric as the transit time team keeps it. */
data class SeasonalTransitTimeMetric(
    var code: String,
    var title: String,
    var amount: Double,
    var endsAt: Int,
    var weight: Double,
    var floorValue: Double,
    var locale: String,
    var createdAt: Int,
    var status: SeasonalTransitTimeMetricStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalTransitTimeMetricStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalTransitTimeMetricStatus): Boolean {
        when (status) {
            SeasonalTransitTimeMetricStatus.OPEN -> {
                when (next) {
                    SeasonalTransitTimeMetricStatus.IN_REVIEW, SeasonalTransitTimeMetricStatus.REOPENED -> return true
                }
                return false
            }
            SeasonalTransitTimeMetricStatus.IN_REVIEW -> {
                when (next) {
                    SeasonalTransitTimeMetricStatus.REOPENED, SeasonalTransitTimeMetricStatus.ARCHIVED -> return true
                }
                return false
            }
            SeasonalTransitTimeMetricStatus.RESOLVED -> {
                when (next) {
                    SeasonalTransitTimeMetricStatus.REOPENED -> return true
                }
                return false
            }
            SeasonalTransitTimeMetricStatus.REOPENED -> {
                when (next) {
                    SeasonalTransitTimeMetricStatus.ARCHIVED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalTransitTimeMetricStatus) {
        if (!canMoveTo(next)) {
            error("seasonal transit time metric $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "${"seasonal transit time metric"} $code"

    fun projectedVolume(): Double = round((endsAt.toDouble() + amount) * 100) / 100

    fun snapshot(): SeasonalTransitTimeMetric {
        return SeasonalTransitTimeMetric(
            code = code,
            title = title,
            amount = amount,
            endsAt = endsAt,
            weight = weight,
            floorValue = floorValue,
            locale = locale,
            createdAt = createdAt,
            status = status,
        )
    }

    fun groupKey(): String = normalize(locale)
}

/** Starts a seasonal transit time metric in status open. */
fun newSeasonalTransitTimeMetric(code: String, title: String): SeasonalTransitTimeMetric {
    return SeasonalTransitTimeMetric(
        code = code,
        title = title,
        amount = 0.0,
        endsAt = 0,
        weight = 0.0,
        floorValue = 0.0,
        locale = "",
        createdAt = 0,
        status = SeasonalTransitTimeMetricStatus.OPEN,
    )
}
