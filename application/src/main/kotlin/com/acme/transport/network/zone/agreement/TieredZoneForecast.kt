package com.acme.transport.network.zone.agreement

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered zone forecast is in its life cycle. */
enum class TieredZoneForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a tiered zone forecast and its current status. */
data class TieredZoneForecast(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var minimum: Double,
    var driverId: String,
    var unit: String,
    var threshold: Double,
    var country: String,
    var ratio: Double,
    var status: TieredZoneForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredZoneForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredZoneForecastStatus): Boolean {
        when (status) {
            TieredZoneForecastStatus.CREATED -> {
                when (next) {
                    TieredZoneForecastStatus.VALIDATED, TieredZoneForecastStatus.RETIRED -> return true
                }
                return false
            }
            TieredZoneForecastStatus.VALIDATED -> {
                when (next) {
                    TieredZoneForecastStatus.PUBLISHED -> return true
                }
                return false
            }
            TieredZoneForecastStatus.PUBLISHED -> {
                when (next) {
                    TieredZoneForecastStatus.RETIRED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: TieredZoneForecastStatus) {
        if (!canMoveTo(next)) {
            error("tiered zone forecast $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedValue(): Double = round(threshold * ratio * 100) / 100

    fun snapshot(): TieredZoneForecast {
        return TieredZoneForecast(
            code = code,
            label = label,
            expiresAt = expiresAt,
            minimum = minimum,
            driverId = driverId,
            unit = unit,
            threshold = threshold,
            country = country,
            ratio = ratio,
            status = status,
        )
    }

    fun groupKey(): String = normalize(unit)
}

/** Starts a tiered zone forecast in status created. */
fun newTieredZoneForecast(code: String, label: String): TieredZoneForecast {
    return TieredZoneForecast(
        code = code,
        label = label,
        expiresAt = 0,
        minimum = 0.0,
        driverId = "",
        unit = "",
        threshold = 0.0,
        country = "",
        ratio = 0.0,
        status = TieredZoneForecastStatus.CREATED,
    )
}
