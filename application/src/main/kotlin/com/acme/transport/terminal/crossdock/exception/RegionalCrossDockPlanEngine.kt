package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional cross dock plan calculation. */
interface RegionalCrossDockPlanFactor {
    fun describe(): String
    fun adjust(item: RegionalCrossDockPlan, value: Double): Double
}

data class LocaleRegionalCrossDockPlanAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalCrossDockPlanFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: RegionalCrossDockPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RegionalCrossDockPlanRetriesOffsetAdjuster(var weight: Double) : RegionalCrossDockPlanFactor {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: RegionalCrossDockPlan, value: Double): Double = value + item.retries.toDouble() * weight
}

data class TieredRegionalCrossDockPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalCrossDockPlanFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalCrossDockPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRegionalCrossDockPlanAdjuster(var step: Double) : RegionalCrossDockPlanFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalCrossDockPlan, value: Double): Double = roundTo(value, step)
}

data class FlaggedRegionalCrossDockPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalCrossDockPlanFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalCrossDockPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalCrossDockPlanAdjuster(var cap: Double) : RegionalCrossDockPlanFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalCrossDockPlan, value: Double): Double = minOf(value, cap)
}

/** Runs the regional cross dock plan steps in order and keeps a trace of each. */
data class RegionalCrossDockPlanEngine(
    var steps: MutableList<RegionalCrossDockPlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalCrossDockPlan): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalCrossDockPlanFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalCrossDockPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalCrossDockPlanEngine(flags: Registry): RegionalCrossDockPlanEngine {
    return RegionalCrossDockPlanEngine(
        steps = mutableListOf<RegionalCrossDockPlanFactor>(
            LocaleRegionalCrossDockPlanAdjuster(
                mutableMapOf<String, Double>("retail" to 1.3, "central" to 0.78, "online" to 1.1),
            ),
            RegionalCrossDockPlanRetriesOffsetAdjuster(0.8),
            TieredRegionalCrossDockPlanAdjuster(
                mutableListOf<Double>(1541.0, 1555.0, 5061.0, 6224.0),
                mutableListOf<Double>(0.86, 0.84, 1.14, 0.83),
            ),
            RoundingRegionalCrossDockPlanAdjuster(1.0),
            FlaggedRegionalCrossDockPlanAdjuster(flags, "regional-cross-dock-plan-legacy", 2.6),
            CapRegionalCrossDockPlanAdjuster(8701.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 1248.1,
    )
}
