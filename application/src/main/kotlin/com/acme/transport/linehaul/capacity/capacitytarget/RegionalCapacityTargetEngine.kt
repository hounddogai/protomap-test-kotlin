package com.acme.transport.linehaul.capacity.capacitytarget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional capacity target calculation. */
interface RegionalCapacityTargetModifier {
    fun describe(): String
    fun adjust(item: RegionalCapacityTarget, value: Double): Double
}

data class RoundingRegionalCapacityTargetAdjuster(var step: Double) : RegionalCapacityTargetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalCapacityTarget, value: Double): Double = roundTo(value, step)
}

data class FlaggedRegionalCapacityTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalCapacityTargetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalCapacityTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRegionalCapacityTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalCapacityTargetModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalCapacityTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the regional capacity target steps in order and keeps a trace of each. */
data class RegionalCapacityTargetEngine(
    var steps: MutableList<RegionalCapacityTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalCapacityTarget): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalCapacityTargetModifier = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalCapacityTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalCapacityTargetEngine(flags: Registry): RegionalCapacityTargetEngine {
    return RegionalCapacityTargetEngine(
        steps = mutableListOf<RegionalCapacityTargetModifier>(
            RoundingRegionalCapacityTargetAdjuster(1.0),
            FlaggedRegionalCapacityTargetAdjuster(flags, "regional-capacity-target-tiered", 0.9),
            TieredRegionalCapacityTargetAdjuster(
                mutableListOf<Double>(6812.0, 8439.0),
                mutableListOf<Double>(0.98, 0.85),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 78923.5,
    )
}
