package com.acme.transport.customs.hold.forecast

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The scheduled hold forecast records of customs's hold work, and the rules, stores, and calculations around
// them.

/** Where a scheduled hold forecast is in its life cycle. */
enum class ScheduledHoldForecastStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** The record of a scheduled hold forecast and its current status. */
data class ScheduledHoldForecast(
    var code: String,
    var label: String,
    var ratio: Double,
    var lane: String,
    var country: String,
    var durationMinutes: Int,
    var startsAt: Int,
    var segment: String,
    var minimum: Double,
    var checksum: String,
    var priority: Int,
    var status: ScheduledHoldForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            ScheduledHoldForecastStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: ScheduledHoldForecastStatus): Boolean {
        when (status) {
            ScheduledHoldForecastStatus.NEW -> return next == ScheduledHoldForecastStatus.RUNNING || next == ScheduledHoldForecastStatus.FAILED
            ScheduledHoldForecastStatus.QUEUED -> {
                when (next) {
                    ScheduledHoldForecastStatus.RUNNING -> return true
                }
                return false
            }
            ScheduledHoldForecastStatus.RUNNING -> {
                when (next) {
                    ScheduledHoldForecastStatus.DONE -> return true
                }
                return false
            }
            ScheduledHoldForecastStatus.DONE -> {
                when (next) {
                    ScheduledHoldForecastStatus.FAILED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: ScheduledHoldForecastStatus) {
        if (!canMoveTo(next)) {
            error("scheduled hold forecast $code cannot move from $status to $next")
        }
        status = next
        startsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code / $label"

    fun weightedTotal(): Double = round(durationMinutes.toDouble() * startsAt.toDouble() * 100) / 100

    fun snapshot(): ScheduledHoldForecast {
        return ScheduledHoldForecast(
            code = code,
            label = label,
            ratio = ratio,
            lane = lane,
            country = country,
            durationMinutes = durationMinutes,
            startsAt = startsAt,
            segment = segment,
            minimum = minimum,
            checksum = checksum,
            priority = priority,
            status = status,
        )
    }

    fun groupKey(): String = normalize(lane)
}

/** Starts a scheduled hold forecast in status new. */
fun newScheduledHoldForecast(code: String, label: String): ScheduledHoldForecast {
    return ScheduledHoldForecast(
        code = code,
        label = label,
        ratio = 0.0,
        lane = "",
        country = "",
        durationMinutes = 0,
        startsAt = 0,
        segment = "",
        minimum = 0.0,
        checksum = "",
        priority = 0,
        status = ScheduledHoldForecastStatus.NEW,
    )
}
