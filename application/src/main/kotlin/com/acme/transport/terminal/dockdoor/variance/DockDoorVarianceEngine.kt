package com.acme.transport.terminal.dockdoor.variance

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the dock door variance calculation. */
interface DockDoorVarianceAdjuster {
    fun describe(): String
    fun adjust(item: DockDoorVariance, value: Double): Double
}

data class DockDoorVarianceWindowDaysOffsetAdjuster(var weight: Double) : DockDoorVarianceAdjuster {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: DockDoorVariance, value: Double): Double = value + item.windowDays.toDouble() * weight
}

data class TieredDockDoorVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorVarianceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumDockDoorVarianceAdjuster(var minimum: Double) : DockDoorVarianceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DockDoorVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the dock door variance steps in order and keeps a trace of each. */
data class DockDoorVarianceEngine(
    var steps: MutableList<DockDoorVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DockDoorVariance): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DockDoorVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDockDoorVarianceEngine(): DockDoorVarianceEngine {
    return DockDoorVarianceEngine(
        steps = mutableListOf<DockDoorVarianceAdjuster>(
            DockDoorVarianceWindowDaysOffsetAdjuster(0.68),
            TieredDockDoorVarianceAdjuster(
                mutableListOf<Double>(2620.0, 3526.0),
                mutableListOf<Double>(0.98, 1.15),
            ),
            MinimumDockDoorVarianceAdjuster(6.2),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 86638.3,
    )
}
