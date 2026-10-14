package com.acme.transport.crewing.roster.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The seasonal roster forecast records of crewing's roster work, and the rules, stores, and calculations
// around them.

/** Where a seasonal roster forecast is in its life cycle. */
enum class SeasonalRosterForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a seasonal roster forecast and its current status. */
data class SeasonalRosterForecast(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var summary: String,
    var source: String,
    var endsAt: Int,
    var weight: Double,
    var segment: String,
    var dock: String,
    var status: SeasonalRosterForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            SeasonalRosterForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: SeasonalRosterForecastStatus): Boolean {
        when (status) {
            SeasonalRosterForecastStatus.CREATED -> {
                when (next) {
                    SeasonalRosterForecastStatus.PUBLISHED, SeasonalRosterForecastStatus.RETIRED -> return true
                }
                return false
            }
            SeasonalRosterForecastStatus.VALIDATED -> {
                when (next) {
                    SeasonalRosterForecastStatus.PUBLISHED -> return true
                }
                return false
            }
            SeasonalRosterForecastStatus.PUBLISHED -> {
                when (next) {
                    SeasonalRosterForecastStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: SeasonalRosterForecastStatus) {
        if (!canMoveTo(next)) {
            error("seasonal roster forecast $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code: $label"

    fun weightedValue(): Double = round((expiresAt.toDouble() + endsAt.toDouble()) * 100) / 100

    fun snapshot(): SeasonalRosterForecast {
        return SeasonalRosterForecast(
            code = code,
            label = label,
            expiresAt = expiresAt,
            summary = summary,
            source = source,
            endsAt = endsAt,
            weight = weight,
            segment = segment,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a seasonal roster forecast in status created. */
fun newSeasonalRosterForecast(code: String, label: String): SeasonalRosterForecast {
    return SeasonalRosterForecast(
        code = code,
        label = label,
        expiresAt = 0,
        summary = "",
        source = "",
        endsAt = 0,
        weight = 0.0,
        segment = "",
        dock = "",
        status = SeasonalRosterForecastStatus.CREATED,
    )
}
