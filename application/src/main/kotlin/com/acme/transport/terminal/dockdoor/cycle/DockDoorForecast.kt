package com.acme.transport.terminal.dockdoor.cycle

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a dock door forecast is in its life cycle. */
enum class DockDoorForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a dock door forecast and its current status. */
data class DockDoorForecast(
    var code: String,
    var name: String,
    var loadKg: Double,
    var budget: Double,
    var maximum: Double,
    var status: DockDoorForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            DockDoorForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: DockDoorForecastStatus): Boolean {
        when (status) {
            DockDoorForecastStatus.PLANNED -> {
                when (next) {
                    DockDoorForecastStatus.SCHEDULED -> return true
                }
                return false
            }
            DockDoorForecastStatus.SCHEDULED -> {
                when (next) {
                    DockDoorForecastStatus.COMPLETED -> return true
                }
                return false
            }
            DockDoorForecastStatus.IN_PROGRESS -> return next == DockDoorForecastStatus.CANCELLED
            DockDoorForecastStatus.COMPLETED -> {
                when (next) {
                    DockDoorForecastStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: DockDoorForecastStatus) {
        if (!canMoveTo(next)) {
            error("dock door forecast $code cannot move from $status to $next")
        }
        status = next
    }

    fun label(): String = "$code: $name"

    fun effectiveVolume(): Double = round(maximum * budget * 100) / 100

    fun snapshot(): DockDoorForecast {
        return DockDoorForecast(
            code = code,
            name = name,
            loadKg = loadKg,
            budget = budget,
            maximum = maximum,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a dock door forecast in status planned. */
fun newDockDoorForecast(code: String, name: String): DockDoorForecast {
    return DockDoorForecast(
        code = code,
        name = name,
        loadKg = 0.0,
        budget = 0.0,
        maximum = 0.0,
        status = DockDoorForecastStatus.PLANNED,
    )
}
