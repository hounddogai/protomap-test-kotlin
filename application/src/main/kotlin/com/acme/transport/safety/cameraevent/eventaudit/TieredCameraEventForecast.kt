package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a tiered camera event forecast is in its life cycle. */
enum class TieredCameraEventForecastStatus {
    CREATED, VALIDATED, PUBLISHED, RETIRED,
}

/** A tiered camera event forecast as the camera event team keeps it. */
data class TieredCameraEventForecast(
    var code: String,
    var name: String,
    var etaMinutes: Int,
    var reviewer: String,
    var externalRef: String,
    var minimum: Double,
    var batchSize: Int,
    var graceDays: Int,
    var active: Boolean,
    var dock: String,
    var status: TieredCameraEventForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            TieredCameraEventForecastStatus.RETIRED -> return true
        }
        return false
    }

    fun canMoveTo(next: TieredCameraEventForecastStatus): Boolean {
        when (status) {
            TieredCameraEventForecastStatus.CREATED -> return next == TieredCameraEventForecastStatus.RETIRED
            TieredCameraEventForecastStatus.VALIDATED -> {
                when (next) {
                    TieredCameraEventForecastStatus.RETIRED -> return true
                }
                return false
            }
            TieredCameraEventForecastStatus.PUBLISHED -> return next == TieredCameraEventForecastStatus.RETIRED
        }
        return false
    }

    fun moveTo(next: TieredCameraEventForecastStatus) {
        if (!canMoveTo(next)) {
            error("tiered camera event forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "${"tiered camera event forecast"} $code"

    fun adjustedVolume(): Double = round(graceDays.toDouble() * etaMinutes.toDouble() * 100) / 100

    fun snapshot(): TieredCameraEventForecast {
        return TieredCameraEventForecast(
            code = code,
            name = name,
            etaMinutes = etaMinutes,
            reviewer = reviewer,
            externalRef = externalRef,
            minimum = minimum,
            batchSize = batchSize,
            graceDays = graceDays,
            active = active,
            dock = dock,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a tiered camera event forecast in status created. */
fun newTieredCameraEventForecast(code: String, name: String): TieredCameraEventForecast {
    return TieredCameraEventForecast(
        code = code,
        name = name,
        etaMinutes = 0,
        reviewer = "",
        externalRef = "",
        minimum = 0.0,
        batchSize = 0,
        graceDays = 0,
        active = false,
        dock = "",
        status = TieredCameraEventForecastStatus.CREATED,
    )
}
