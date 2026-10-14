package com.acme.transport.network.servicearea.areabatch

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a fallback service area forecast is in its life cycle. */
enum class FallbackServiceAreaForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** The record of a fallback service area forecast and its current status. */
data class FallbackServiceAreaForecast(
    var code: String,
    var title: String,
    var category: String,
    var pallets: Int,
    var unit: String,
    var floorValue: Double,
    var expiresAt: Int,
    var source: String,
    var score: Double,
    var status: FallbackServiceAreaForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FallbackServiceAreaForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: FallbackServiceAreaForecastStatus): Boolean {
        when (status) {
            FallbackServiceAreaForecastStatus.CREATED -> {
                when (next) {
                    FallbackServiceAreaForecastStatus.PUBLISHED, FallbackServiceAreaForecastStatus.RETIRED -> return true
                }
                return false
            }
            FallbackServiceAreaForecastStatus.VALIDATED -> {
                when (next) {
                    FallbackServiceAreaForecastStatus.PUBLISHED, FallbackServiceAreaForecastStatus.RETIRED -> return true
                }
                return false
            }
            FallbackServiceAreaForecastStatus.PUBLISHED -> return next == FallbackServiceAreaForecastStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: FallbackServiceAreaForecastStatus) {
        if (!canMoveTo(next)) {
            error("fallback service area forecast $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code [$title]"

    fun adjustedExposure(): Double = round((floorValue + expiresAt.toDouble()) * 100) / 100

    fun snapshot(): FallbackServiceAreaForecast {
        return FallbackServiceAreaForecast(
            code = code,
            title = title,
            category = category,
            pallets = pallets,
            unit = unit,
            floorValue = floorValue,
            expiresAt = expiresAt,
            source = source,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(category)
}

/** Starts a fallback service area forecast in status created. */
fun newFallbackServiceAreaForecast(code: String, title: String): FallbackServiceAreaForecast {
    return FallbackServiceAreaForecast(
        code = code,
        title = title,
        category = "",
        pallets = 0,
        unit = "",
        floorValue = 0.0,
        expiresAt = 0,
        source = "",
        score = 0.0,
        status = FallbackServiceAreaForecastStatus.CREATED,
    )
}
