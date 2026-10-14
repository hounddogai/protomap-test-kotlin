package com.acme.transport.safety.cameraevent.eventscore

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a weighted camera event forecast is in its life cycle. */
enum class WeightedCameraEventForecastStatus {
    OPEN, IN_REVIEW, RESOLVED, REOPENED, ARCHIVED,
}

/** A weighted camera event forecast. Its code is unique within a tenant. */
data class WeightedCameraEventForecast(
    var code: String,
    var title: String,
    var timezone: String,
    var createdAt: Int,
    var score: Double,
    var loadKg: Double,
    var ratio: Double,
    var windowDays: Int,
    var maximum: Double,
    var durationMinutes: Int,
    var pinned: Boolean,
    var status: WeightedCameraEventForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedCameraEventForecastStatus.ARCHIVED -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedCameraEventForecastStatus): Boolean {
        when (status) {
            WeightedCameraEventForecastStatus.OPEN -> {
                when (next) {
                    WeightedCameraEventForecastStatus.REOPENED, WeightedCameraEventForecastStatus.ARCHIVED -> return true
                }
                return false
            }
            WeightedCameraEventForecastStatus.IN_REVIEW -> {
                when (next) {
                    WeightedCameraEventForecastStatus.REOPENED -> return true
                }
                return false
            }
            WeightedCameraEventForecastStatus.RESOLVED -> return next == WeightedCameraEventForecastStatus.REOPENED || next == WeightedCameraEventForecastStatus.ARCHIVED
            WeightedCameraEventForecastStatus.REOPENED -> return next == WeightedCameraEventForecastStatus.ARCHIVED
        }
        return false
    }

    fun moveTo(next: WeightedCameraEventForecastStatus) {
        if (!canMoveTo(next)) {
            error("weighted camera event forecast $code cannot move from $status to $next")
        }
        status = next
        createdAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun label(): String = "$code / $title"

    fun projectedTotal(): Double = round((createdAt.toDouble() - loadKg) * 100) / 100

    fun snapshot(): WeightedCameraEventForecast {
        return WeightedCameraEventForecast(
            code = code,
            title = title,
            timezone = timezone,
            createdAt = createdAt,
            score = score,
            loadKg = loadKg,
            ratio = ratio,
            windowDays = windowDays,
            maximum = maximum,
            durationMinutes = durationMinutes,
            pinned = pinned,
            status = status,
        )
    }

    fun groupKey(): String = normalize(title)
}

/** Starts a weighted camera event forecast in status open. */
fun newWeightedCameraEventForecast(code: String, title: String): WeightedCameraEventForecast {
    return WeightedCameraEventForecast(
        code = code,
        title = title,
        timezone = "",
        createdAt = 0,
        score = 0.0,
        loadKg = 0.0,
        ratio = 0.0,
        windowDays = 0,
        maximum = 0.0,
        durationMinutes = 0,
        pinned = false,
        status = WeightedCameraEventForecastStatus.OPEN,
    )
}
