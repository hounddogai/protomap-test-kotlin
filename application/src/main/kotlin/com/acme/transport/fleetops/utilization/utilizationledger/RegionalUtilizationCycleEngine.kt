package com.acme.transport.fleetops.utilization.utilizationledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional utilization cycle calculation. */
interface RegionalUtilizationCycleAdjuster {
    fun describe(): String
    fun adjust(item: RegionalUtilizationCycle, value: Double): Double
}

data class TieredRegionalUtilizationCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalUtilizationCycleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalUtilizationCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRegionalUtilizationCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalUtilizationCycleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalUtilizationCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRegionalUtilizationCycleAdjuster(var percent: Double) : RegionalUtilizationCycleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalUtilizationCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleRegionalUtilizationCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalUtilizationCycleAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: RegionalUtilizationCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRegionalUtilizationCycleAdjuster(var step: Double) : RegionalUtilizationCycleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalUtilizationCycle, value: Double): Double = roundTo(value, step)
}

data class CapRegionalUtilizationCycleAdjuster(var cap: Double) : RegionalUtilizationCycleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalUtilizationCycle, value: Double): Double = minOf(value, cap)
}

/** Runs the regional utilization cycle steps in order and keeps a trace of each. */
data class RegionalUtilizationCycleEngine(
    var steps: MutableList<RegionalUtilizationCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalUtilizationCycle): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalUtilizationCycleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalUtilizationCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalUtilizationCycleEngine(flags: Registry): RegionalUtilizationCycleEngine {
    return RegionalUtilizationCycleEngine(
        steps = mutableListOf<RegionalUtilizationCycleAdjuster>(
            TieredRegionalUtilizationCycleAdjuster(
                mutableListOf<Double>(1612.0, 1748.0, 2509.0, 7777.0),
                mutableListOf<Double>(1.06, 1.07, 0.99, 1.03),
            ),
            FlaggedRegionalUtilizationCycleAdjuster(flags, "regional-utilization-cycle-capped", 9.8),
            PercentRegionalUtilizationCycleAdjuster(24.7),
            TitleRegionalUtilizationCycleAdjuster(
                mutableMapOf<String, Double>("central" to 0.81, "online" to 0.84),
            ),
            RoundingRegionalUtilizationCycleAdjuster(0.05),
            CapRegionalUtilizationCycleAdjuster(5866.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 40726.6,
    )
}
