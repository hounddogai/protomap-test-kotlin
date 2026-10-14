package com.acme.transport.emissions.fuelburn.estimate

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional fuel burn threshold calculation. */
interface RegionalFuelBurnThresholdAdjuster {
    fun describe(): String
    fun adjust(item: RegionalFuelBurnThreshold, value: Double): Double
}

data class RegionalFuelBurnThresholdCapacityOffsetAdjuster(
    var weight: Double,
) : RegionalFuelBurnThresholdAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: RegionalFuelBurnThreshold, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class TieredRegionalFuelBurnThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalFuelBurnThresholdAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalFuelBurnThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRegionalFuelBurnThresholdAdjuster(var step: Double) : RegionalFuelBurnThresholdAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalFuelBurnThreshold, value: Double): Double = roundTo(value, step)
}

data class CapRegionalFuelBurnThresholdAdjuster(var cap: Double) : RegionalFuelBurnThresholdAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalFuelBurnThreshold, value: Double): Double = minOf(value, cap)
}

data class ChannelRegionalFuelBurnThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalFuelBurnThresholdAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: RegionalFuelBurnThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional fuel burn threshold steps in order and keeps a trace of each. */
data class RegionalFuelBurnThresholdEngine(
    var steps: MutableList<RegionalFuelBurnThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalFuelBurnThreshold): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalFuelBurnThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalFuelBurnThresholdEngine(): RegionalFuelBurnThresholdEngine {
    return RegionalFuelBurnThresholdEngine(
        steps = mutableListOf<RegionalFuelBurnThresholdAdjuster>(
            RegionalFuelBurnThresholdCapacityOffsetAdjuster(0.69),
            TieredRegionalFuelBurnThresholdAdjuster(
                mutableListOf<Double>(5218.0, 9301.0),
                mutableListOf<Double>(0.91, 1.04),
            ),
            RoundingRegionalFuelBurnThresholdAdjuster(1.0),
            CapRegionalFuelBurnThresholdAdjuster(10778.0),
            ChannelRegionalFuelBurnThresholdAdjuster(
                mutableMapOf<String, Double>("online" to 1.19, "west" to 1.3, "central" to 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 26684.0,
    )
}
