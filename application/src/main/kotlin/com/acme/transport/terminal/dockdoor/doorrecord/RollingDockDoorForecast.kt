package com.acme.transport.terminal.dockdoor.doorrecord

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a rolling dock door forecast is in its life cycle. */
enum class RollingDockDoorForecastStatus {
    NEW, QUEUED, RUNNING, DONE, FAILED,
}

/** A rolling dock door forecast as the dock door team keeps it. */
data class RollingDockDoorForecast(
    var code: String,
    var label: String,
    var summary: String,
    var batchSize: Int,
    var description: String,
    var stopCount: Int,
    var endsAt: Int,
    var ceiling: Double,
    var score: Double,
    var status: RollingDockDoorForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RollingDockDoorForecastStatus.FAILED -> return true
        }
        return false
    }

    fun canMoveTo(next: RollingDockDoorForecastStatus): Boolean {
        when (status) {
            RollingDockDoorForecastStatus.NEW -> return next == RollingDockDoorForecastStatus.QUEUED
            RollingDockDoorForecastStatus.QUEUED -> return next == RollingDockDoorForecastStatus.RUNNING || next == RollingDockDoorForecastStatus.DONE
            RollingDockDoorForecastStatus.RUNNING -> return next == RollingDockDoorForecastStatus.DONE || next == RollingDockDoorForecastStatus.FAILED
            RollingDockDoorForecastStatus.DONE -> return next == RollingDockDoorForecastStatus.FAILED
        }
        return false
    }

    fun moveTo(next: RollingDockDoorForecastStatus) {
        if (!canMoveTo(next)) {
            error("rolling dock door forecast $code cannot move from $status to $next")
        }
        status = next
        endsAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "${"rolling dock door forecast"} $code"

    fun netExposure(): Double = round(endsAt.toDouble() * score * 100) / 100

    fun snapshot(): RollingDockDoorForecast {
        return RollingDockDoorForecast(
            code = code,
            label = label,
            summary = summary,
            batchSize = batchSize,
            description = description,
            stopCount = stopCount,
            endsAt = endsAt,
            ceiling = ceiling,
            score = score,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a rolling dock door forecast in status new. */
fun newRollingDockDoorForecast(code: String, label: String): RollingDockDoorForecast {
    return RollingDockDoorForecast(
        code = code,
        label = label,
        summary = "",
        batchSize = 0,
        description = "",
        stopCount = 0,
        endsAt = 0,
        ceiling = 0.0,
        score = 0.0,
        status = RollingDockDoorForecastStatus.NEW,
    )
}
