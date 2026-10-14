package com.acme.transport.emissions.evcharge.pendingchargescore

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the ev charge rollup calculation. */
interface EvChargeRollupModifier {
    fun describe(): String
    fun adjust(item: EvChargeRollup, value: Double): Double
}

data class NameEvChargeRollupAdjuster(var factors: MutableMap<String, Double>) : EvChargeRollupModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: EvChargeRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingEvChargeRollupAdjuster(var step: Double) : EvChargeRollupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: EvChargeRollup, value: Double): Double = roundTo(value, step)
}

data class MinimumEvChargeRollupAdjuster(var minimum: Double) : EvChargeRollupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EvChargeRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedEvChargeRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : EvChargeRollupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: EvChargeRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredEvChargeRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EvChargeRollupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EvChargeRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class EvChargeRollupMinimumOffsetAdjuster(var weight: Double) : EvChargeRollupModifier {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: EvChargeRollup, value: Double): Double = value + item.minimum * weight
}

/** Runs the ev charge rollup steps in order and keeps a trace of each. */
data class EvChargeRollupEngine(
    var steps: MutableList<EvChargeRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: EvChargeRollup): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EvChargeRollupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<EvChargeRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultEvChargeRollupEngine(flags: Registry): EvChargeRollupEngine {
    return EvChargeRollupEngine(
        steps = mutableListOf<EvChargeRollupModifier>(
            NameEvChargeRollupAdjuster(mutableMapOf<String, Double>("east" to 0.96, "online" to 1.06)),
            RoundingEvChargeRollupAdjuster(0.25),
            MinimumEvChargeRollupAdjuster(25.1),
            FlaggedEvChargeRollupAdjuster(flags, "ev-charge-rollup-adaptive", 1.8),
            TieredEvChargeRollupAdjuster(
                mutableListOf<Double>(1959.0, 6217.0, 7226.0),
                mutableListOf<Double>(1.09, 1.11, 0.95),
            ),
            EvChargeRollupMinimumOffsetAdjuster(0.36),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 58611.1,
    )
}
