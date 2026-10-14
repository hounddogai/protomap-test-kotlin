package com.acme.transport.terminal.dwell.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal dwell policy calculation. */
interface SeasonalDwellPolicyAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalDwellPolicy, value: Double): Double
}

data class CapSeasonalDwellPolicyAdjuster(var cap: Double) : SeasonalDwellPolicyAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalDwellPolicy, value: Double): Double = minOf(value, cap)
}

data class RoundingSeasonalDwellPolicyAdjuster(var step: Double) : SeasonalDwellPolicyAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalDwellPolicy, value: Double): Double = roundTo(value, step)
}

data class PercentSeasonalDwellPolicyAdjuster(var percent: Double) : SeasonalDwellPolicyAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalDwellPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class SourceSeasonalDwellPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalDwellPolicyAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: SeasonalDwellPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedSeasonalDwellPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalDwellPolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalDwellPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSeasonalDwellPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalDwellPolicyAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalDwellPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the seasonal dwell policy steps in order and keeps a trace of each. */
data class SeasonalDwellPolicyEngine(
    var steps: MutableList<SeasonalDwellPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalDwellPolicy): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalDwellPolicyAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalDwellPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalDwellPolicyEngine(flags: Registry): SeasonalDwellPolicyEngine {
    return SeasonalDwellPolicyEngine(
        steps = mutableListOf<SeasonalDwellPolicyAdjuster>(
            CapSeasonalDwellPolicyAdjuster(14323.0),
            RoundingSeasonalDwellPolicyAdjuster(0.1),
            PercentSeasonalDwellPolicyAdjuster(18.9),
            SourceSeasonalDwellPolicyAdjuster(mutableMapOf<String, Double>("east" to 1.27, "online" to 1.24)),
            FlaggedSeasonalDwellPolicyAdjuster(flags, "seasonal-dwell-policy-strict", -3.9),
            TieredSeasonalDwellPolicyAdjuster(
                mutableListOf<Double>(2280.0, 2432.0, 5358.0, 5367.0),
                mutableListOf<Double>(1.19, 1.17, 0.97, 0.89),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 58531.0,
    )
}
