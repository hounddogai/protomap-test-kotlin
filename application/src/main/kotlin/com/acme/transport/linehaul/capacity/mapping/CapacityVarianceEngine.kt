package com.acme.transport.linehaul.capacity.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capacity variance calculation. */
interface CapacityVarianceModifier {
    fun describe(): String
    fun adjust(item: CapacityVariance, value: Double): Double
}

data class FlaggedCapacityVarianceAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CapacityVarianceModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CapacityVariance, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCapacityVarianceAdjuster(var percent: Double) : CapacityVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CapacityVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumCapacityVarianceAdjuster(var minimum: Double) : CapacityVarianceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CapacityVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelCapacityVarianceAdjuster(var factors: MutableMap<String, Double>) : CapacityVarianceModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: CapacityVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredCapacityVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CapacityVarianceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CapacityVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCapacityVarianceAdjuster(var step: Double) : CapacityVarianceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CapacityVariance, value: Double): Double = roundTo(value, step)
}

/** Runs the capacity variance steps in order and keeps a trace of each. */
data class CapacityVarianceEngine(
    var steps: MutableList<CapacityVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CapacityVariance): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CapacityVarianceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CapacityVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCapacityVarianceEngine(flags: Registry): CapacityVarianceEngine {
    return CapacityVarianceEngine(
        steps = mutableListOf<CapacityVarianceModifier>(
            FlaggedCapacityVarianceAdjuster(flags, "capacity-variance-legacy", -3.1),
            PercentCapacityVarianceAdjuster(15.6),
            MinimumCapacityVarianceAdjuster(32.6),
            LabelCapacityVarianceAdjuster(mutableMapOf<String, Double>("north" to 0.96, "central" to 0.91)),
            TieredCapacityVarianceAdjuster(
                mutableListOf<Double>(2370.0, 2660.0, 6165.0),
                mutableListOf<Double>(1.02, 1.19, 1.03),
            ),
            RoundingCapacityVarianceAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 32219.1,
    )
}
