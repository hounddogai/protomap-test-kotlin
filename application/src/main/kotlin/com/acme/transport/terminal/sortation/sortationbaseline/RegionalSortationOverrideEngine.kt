package com.acme.transport.terminal.sortation.sortationbaseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional sortation override calculation. */
interface RegionalSortationOverrideModifier {
    fun describe(): String
    fun adjust(item: RegionalSortationOverride, value: Double): Double
}

data class CapRegionalSortationOverrideAdjuster(var cap: Double) : RegionalSortationOverrideModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalSortationOverride, value: Double): Double = minOf(value, cap)
}

data class PercentRegionalSortationOverrideAdjuster(var percent: Double) : RegionalSortationOverrideModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalSortationOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRegionalSortationOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalSortationOverrideModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalSortationOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameRegionalSortationOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalSortationOverrideModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalSortationOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRegionalSortationOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalSortationOverrideModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalSortationOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRegionalSortationOverrideAdjuster(var step: Double) : RegionalSortationOverrideModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalSortationOverride, value: Double): Double = roundTo(value, step)
}

/** Runs the regional sortation override steps in order and keeps a trace of each. */
data class RegionalSortationOverrideEngine(
    var steps: MutableList<RegionalSortationOverrideModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalSortationOverride): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalSortationOverrideModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalSortationOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalSortationOverrideEngine(flags: Registry): RegionalSortationOverrideEngine {
    return RegionalSortationOverrideEngine(
        steps = mutableListOf<RegionalSortationOverrideModifier>(
            CapRegionalSortationOverrideAdjuster(30867.0),
            PercentRegionalSortationOverrideAdjuster(9.4),
            FlaggedRegionalSortationOverrideAdjuster(flags, "regional-sortation-override-capped", 5.6),
            NameRegionalSortationOverrideAdjuster(mutableMapOf<String, Double>("north" to 0.96)),
            TieredRegionalSortationOverrideAdjuster(
                mutableListOf<Double>(2639.0, 6011.0),
                mutableListOf<Double>(1.12, 1.02),
            ),
            RoundingRegionalSortationOverrideAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 13576.6,
    )
}
