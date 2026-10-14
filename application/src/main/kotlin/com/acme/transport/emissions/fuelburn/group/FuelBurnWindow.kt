package com.acme.transport.emissions.fuelburn.group

import com.acme.transport.kit.text.normalize

/** Where a fuel burn window is in its life cycle. */
enum class FuelBurnWindowStatus {
    PLANNED, SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED,
}

/** The record of a fuel burn window and its current status. */
data class FuelBurnWindow(
    var code: String,
    var title: String,
    var source: String,
    var description: String,
    var label: String,
    var margin: Double,
    var visible: Boolean,
    var status: FuelBurnWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            FuelBurnWindowStatus.CANCELLED -> return true
        }
        return false
    }

    fun canMoveTo(next: FuelBurnWindowStatus): Boolean {
        when (status) {
            FuelBurnWindowStatus.PLANNED -> return next == FuelBurnWindowStatus.COMPLETED || next == FuelBurnWindowStatus.CANCELLED
            FuelBurnWindowStatus.SCHEDULED -> return next == FuelBurnWindowStatus.IN_PROGRESS
            FuelBurnWindowStatus.IN_PROGRESS -> {
                when (next) {
                    FuelBurnWindowStatus.COMPLETED, FuelBurnWindowStatus.CANCELLED -> return true
                }
                return false
            }
            FuelBurnWindowStatus.COMPLETED -> {
                when (next) {
                    FuelBurnWindowStatus.CANCELLED -> return true
                }
                return false
            }
        }
        return false
    }

    fun moveTo(next: FuelBurnWindowStatus) {
        if (!canMoveTo(next)) {
            error("fuel burn window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code ($title)"

    fun measuredValue(): Double = margin

    fun snapshot(): FuelBurnWindow {
        return FuelBurnWindow(
            code = code,
            title = title,
            source = source,
            description = description,
            label = label,
            margin = margin,
            visible = visible,
            status = status,
        )
    }

    fun groupKey(): String = normalize(source)
}

/** Starts a fuel burn window in status planned. */
fun newFuelBurnWindow(code: String, title: String): FuelBurnWindow {
    return FuelBurnWindow(
        code = code,
        title = title,
        source = "",
        description = "",
        label = "",
        margin = 0.0,
        visible = false,
        status = FuelBurnWindowStatus.PLANNED,
    )
}
