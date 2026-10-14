package com.acme.transport.terminal.crossdock.dockestimate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional cross dock estimate calculation. */
interface RegionalCrossDockEstimateModifier {
    fun describe(): String
    fun adjust(item: RegionalCrossDockEstimate, value: Double): Double
}

data class FlaggedRegionalCrossDockEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalCrossDockEstimateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalCrossDockEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRegionalCrossDockEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalCrossDockEstimateModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalCrossDockEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentRegionalCrossDockEstimateAdjuster(var percent: Double) : RegionalCrossDockEstimateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalCrossDockEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRegionalCrossDockEstimateAdjuster(var minimum: Double) : RegionalCrossDockEstimateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalCrossDockEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SegmentRegionalCrossDockEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalCrossDockEstimateModifier {
    override fun describe(): String = "by segment"

    override fun adjust(item: RegionalCrossDockEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRegionalCrossDockEstimateAdjuster(var step: Double) : RegionalCrossDockEstimateModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalCrossDockEstimate, value: Double): Double = roundTo(value, step)
}

/** Runs the regional cross dock estimate steps in order and keeps a trace of each. */
data class RegionalCrossDockEstimateEngine(
    var steps: MutableList<RegionalCrossDockEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalCrossDockEstimate): Double {
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

    fun evaluateAll(items: MutableList<RegionalCrossDockEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalCrossDockEstimateEngine(flags: Registry): RegionalCrossDockEstimateEngine {
    return RegionalCrossDockEstimateEngine(
        steps = mutableListOf<RegionalCrossDockEstimateModifier>(
            FlaggedRegionalCrossDockEstimateAdjuster(flags, "regional-cross-dock-estimate-adaptive", 1.5),
            TieredRegionalCrossDockEstimateAdjuster(
                mutableListOf<Double>(384.0, 6556.0, 6939.0, 8019.0),
                mutableListOf<Double>(0.83, 0.87, 0.9, 1.07),
            ),
            PercentRegionalCrossDockEstimateAdjuster(3.8),
            MinimumRegionalCrossDockEstimateAdjuster(4.3),
            SegmentRegionalCrossDockEstimateAdjuster(mutableMapOf<String, Double>("west" to 1.2)),
            RoundingRegionalCrossDockEstimateAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 70019.3,
    )
}
