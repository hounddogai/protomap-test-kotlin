package com.acme.transport.linehaul.capacity.allocation

import com.acme.transport.kit.flags.Registry

/** One step of the capacity baseline calculation. */
interface CapacityBaselineStep {
    fun describe(): String
    fun adjust(item: CapacityBaseline, value: Double): Double
}

data class RegionCapacityBaselineAdjuster(var factors: MutableMap<String, Double>) : CapacityBaselineStep {
    override fun describe(): String = "by region"

    override fun adjust(item: CapacityBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapCapacityBaselineAdjuster(var cap: Double) : CapacityBaselineStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityBaseline, value: Double): Double = minOf(value, cap)
}

data class FlaggedCapacityBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CapacityBaselineStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CapacityBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumCapacityBaselineAdjuster(var minimum: Double) : CapacityBaselineStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CapacityBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the capacity baseline steps in order and keeps a trace of each. */
data class CapacityBaselineEngine(
    var steps: MutableList<CapacityBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CapacityBaseline): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CapacityBaselineStep = steps[index]
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

    fun evaluateAll(items: MutableList<CapacityBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCapacityBaselineEngine(flags: Registry): CapacityBaselineEngine {
    return CapacityBaselineEngine(
        steps = mutableListOf<CapacityBaselineStep>(
            RegionCapacityBaselineAdjuster(mutableMapOf<String, Double>("retail" to 0.95)),
            CapCapacityBaselineAdjuster(23384.0),
            FlaggedCapacityBaselineAdjuster(flags, "capacity-baseline-tiered", 9.4),
            MinimumCapacityBaselineAdjuster(34.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 32131.1,
    )
}
