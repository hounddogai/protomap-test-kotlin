package com.acme.transport.linehaul.backhaul.backhauldigest

import com.acme.transport.kit.text.normalize

/** Where a weighted backhaul forecast is in its life cycle. */
enum class WeightedBackhaulForecastStatus {
    REQUESTED, GRANTED, REVOKED, EXPIRED,
}

/** A weighted backhaul forecast. Its code is unique within a tenant. */
data class WeightedBackhaulForecast(
    var code: String,
    var title: String,
    var margin: Double,
    var reviewer: String,
    var name: String,
    var status: WeightedBackhaulForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedBackhaulForecastStatus.EXPIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedBackhaulForecastStatus): Boolean {
        when (status) {
            WeightedBackhaulForecastStatus.REQUESTED -> {
                when (next) {
                    WeightedBackhaulForecastStatus.REVOKED -> return true
                }
                return false
            }
            WeightedBackhaulForecastStatus.GRANTED -> {
                when (next) {
                    WeightedBackhaulForecastStatus.REVOKED -> return true
                }
                return false
            }
            WeightedBackhaulForecastStatus.REVOKED -> {
                when (next) {
                    WeightedBackhaulForecastStatus.EXPIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: WeightedBackhaulForecastStatus) {
        if (!canMoveTo(next)) {
            error("weighted backhaul forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"weighted backhaul forecast"} $code"

    fun measuredValue(): Double = margin

    fun snapshot(): WeightedBackhaulForecast {
        return WeightedBackhaulForecast(
            code = code,
            title = title,
            margin = margin,
            reviewer = reviewer,
            name = name,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted backhaul forecast in status requested. */
fun newWeightedBackhaulForecast(code: String, title: String): WeightedBackhaulForecast {
    return WeightedBackhaulForecast(
        code = code,
        title = title,
        margin = 0.0,
        reviewer = "",
        name = "",
        status = WeightedBackhaulForecastStatus.REQUESTED,
    )
}
