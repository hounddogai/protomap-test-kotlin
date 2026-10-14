package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fuel burn segment calculation. */
interface FuelBurnSegmentStep {
    fun describe(): String
    fun adjust(item: FuelBurnSegment, value: Double): Double
}

data class FlaggedFuelBurnSegmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FuelBurnSegmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FuelBurnSegment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFuelBurnSegmentAdjuster(var minimum: Double) : FuelBurnSegmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FuelBurnSegment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredFuelBurnSegmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FuelBurnSegmentStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FuelBurnSegment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapFuelBurnSegmentAdjuster(var cap: Double) : FuelBurnSegmentStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FuelBurnSegment, value: Double): Double = minOf(value, cap)
}

data class PercentFuelBurnSegmentAdjuster(var percent: Double) : FuelBurnSegmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FuelBurnSegment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingFuelBurnSegmentAdjuster(var step: Double) : FuelBurnSegmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FuelBurnSegment, value: Double): Double = roundTo(value, step)
}

/** Runs the fuel burn segment steps in order and keeps a trace of each. */
data class FuelBurnSegmentEngine(
    var steps: MutableList<FuelBurnSegmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FuelBurnSegment): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FuelBurnSegment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFuelBurnSegmentEngine(flags: Registry): FuelBurnSegmentEngine {
    return FuelBurnSegmentEngine(
        steps = mutableListOf<FuelBurnSegmentStep>(
            FlaggedFuelBurnSegmentAdjuster(flags, "fuel-burn-segment-adaptive", 3.1),
            MinimumFuelBurnSegmentAdjuster(36.0),
            TieredFuelBurnSegmentAdjuster(
                mutableListOf<Double>(2950.0, 9053.0, 9173.0, 9842.0),
                mutableListOf<Double>(1.0, 1.17, 1.17, 1.04),
            ),
            CapFuelBurnSegmentAdjuster(23669.0),
            PercentFuelBurnSegmentAdjuster(-2.9),
            RoundingFuelBurnSegmentAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 54866.4,
    )
}
