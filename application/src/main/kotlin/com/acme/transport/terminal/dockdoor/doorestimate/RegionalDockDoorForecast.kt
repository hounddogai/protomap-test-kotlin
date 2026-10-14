package com.acme.transport.terminal.dockdoor.doorestimate

import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Where a regional dock door forecast is in its life cycle. */
enum class RegionalDockDoorForecastStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** A regional dock door forecast. Its code is unique within a tenant. */
data class RegionalDockDoorForecast(
    var code: String,
    var label: String,
    var expiresAt: Int,
    var budget: Double,
    var margin: Double,
    var status: RegionalDockDoorForecastStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            RegionalDockDoorForecastStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: RegionalDockDoorForecastStatus): Boolean {
        when (status) {
            RegionalDockDoorForecastStatus.PLANNED -> return next == RegionalDockDoorForecastStatus.IN_PROGRESS || next == RegionalDockDoorForecastStatus.CANCELLED
            RegionalDockDoorForecastStatus.SCHEDULED -> {
                when (next) {
                    RegionalDockDoorForecastStatus.COMPLETED, RegionalDockDoorForecastStatus.CANCELLED -> return true
                }
                return false
            }
            RegionalDockDoorForecastStatus.IN_PROGRESS -> return next == RegionalDockDoorForecastStatus.CANCELLED
            RegionalDockDoorForecastStatus.COMPLETED -> {
                when (next) {
                    RegionalDockDoorForecastStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: RegionalDockDoorForecastStatus) {
        if (!canMoveTo(next)) {
            error("regional dock door forecast $code cannot move from $status to $next")
        }
        status = next
        expiresAt = (System.currentTimeMillis() / 1000).toInt()
    }

    fun displayLabel(): String = "$code [$label]"

    fun effectiveValue(): Double = round(expiresAt.toDouble() * margin * 100) / 100

    fun snapshot(): RegionalDockDoorForecast {
        return RegionalDockDoorForecast(
            code = code,
            label = label,
            expiresAt = expiresAt,
            budget = budget,
            margin = margin,
            status = status,
        )
    }

    fun groupKey(): String = normalize(label)
}

/** Starts a regional dock door forecast in status planned. */
fun newRegionalDockDoorForecast(code: String, label: String): RegionalDockDoorForecast {
    return RegionalDockDoorForecast(
        code = code,
        label = label,
        expiresAt = 0,
        budget = 0.0,
        margin = 0.0,
        status = RegionalDockDoorForecastStatus.PLANNED,
    )
}
