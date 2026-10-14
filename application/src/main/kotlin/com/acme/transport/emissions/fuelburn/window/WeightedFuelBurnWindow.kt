package com.acme.transport.emissions.fuelburn.window

import com.acme.transport.kit.text.normalize
import kotlin.math.round

// The weighted fuel burn window records of emissions's fuel burn work, and the rules, stores, and
// calculations around them.

/** Where a weighted fuel burn window is in its life cycle. */
enum class WeightedFuelBurnWindowStatus {
    PROPOSED, APPROVED, REJECTED, WITHDRAWN,
}

/** A weighted fuel burn window. Its code is unique within a tenant. */
data class WeightedFuelBurnWindow(
    var code: String,
    var name: String,
    var margin: Double,
    var discount: Double,
    var weight: Double,
    var label: String,
    var active: Boolean,
    var floorValue: Double,
    var status: WeightedFuelBurnWindowStatus,
) {
    /** Reports whether the record can no longer change status. */
    fun isTerminal(): Boolean {
        when (status) {
            WeightedFuelBurnWindowStatus.WITHDRAWN -> return true
        }
        return false
    }

    fun canMoveTo(next: WeightedFuelBurnWindowStatus): Boolean {
        when (status) {
            WeightedFuelBurnWindowStatus.PROPOSED -> {
                when (next) {
                    WeightedFuelBurnWindowStatus.REJECTED, WeightedFuelBurnWindowStatus.WITHDRAWN -> return true
                }
                return false
            }
            WeightedFuelBurnWindowStatus.APPROVED -> return next == WeightedFuelBurnWindowStatus.REJECTED || next == WeightedFuelBurnWindowStatus.WITHDRAWN
            WeightedFuelBurnWindowStatus.REJECTED -> return next == WeightedFuelBurnWindowStatus.WITHDRAWN
        }
        return false
    }

    fun moveTo(next: WeightedFuelBurnWindowStatus) {
        if (!canMoveTo(next)) {
            error("weighted fuel burn window $code cannot move from $status to $next")
        }
        status = next
    }

    fun displayLabel(): String = "$code: $name"

    fun netValue(): Double = round((weight + margin) * 100) / 100

    fun snapshot(): WeightedFuelBurnWindow {
        return WeightedFuelBurnWindow(
            code = code,
            name = name,
            margin = margin,
            discount = discount,
            weight = weight,
            label = label,
            active = active,
            floorValue = floorValue,
            status = status,
        )
    }

    fun groupKey(): String = normalize(name)
}

/** Starts a weighted fuel burn window in status proposed. */
fun newWeightedFuelBurnWindow(code: String, name: String): WeightedFuelBurnWindow {
    return WeightedFuelBurnWindow(
        code = code,
        name = name,
        margin = 0.0,
        discount = 0.0,
        weight = 0.0,
        label = "",
        active = false,
        floorValue = 0.0,
        status = WeightedFuelBurnWindowStatus.PROPOSED,
    )
}
