package com.acme.transport.emissions.fuelburn.budget

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fuel burn snapshot calculation. */
interface FuelBurnSnapshotFactor {
    fun describe(): String
    fun adjust(item: FuelBurnSnapshot, value: Double): Double
}

data class FuelBurnSnapshotGraceDaysOffsetAdjuster(var weight: Double) : FuelBurnSnapshotFactor {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: FuelBurnSnapshot, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class MinimumFuelBurnSnapshotAdjuster(var minimum: Double) : FuelBurnSnapshotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFuelBurnSnapshotAdjuster(var step: Double) : FuelBurnSnapshotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnSnapshot, value: Double): Double = roundTo(value, step)
}

data class CapFuelBurnSnapshotAdjuster(var cap: Double) : FuelBurnSnapshotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnSnapshot, value: Double): Double = minOf(value, cap)
}

/** Runs the fuel burn snapshot steps in order and keeps a trace of each. */
data class FuelBurnSnapshotEngine(
    var steps: MutableList<FuelBurnSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: FuelBurnSnapshot): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FuelBurnSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultFuelBurnSnapshotEngine(): FuelBurnSnapshotEngine {
    return FuelBurnSnapshotEngine(
        steps = mutableListOf<FuelBurnSnapshotFactor>(
            FuelBurnSnapshotGraceDaysOffsetAdjuster(0.28),
            MinimumFuelBurnSnapshotAdjuster(48.7),
            RoundingFuelBurnSnapshotAdjuster(0.25),
            CapFuelBurnSnapshotAdjuster(17997.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 18847.3,
    )
}
