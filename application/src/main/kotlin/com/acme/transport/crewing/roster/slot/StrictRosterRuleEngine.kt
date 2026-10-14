package com.acme.transport.crewing.roster.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict roster rule calculation. */
interface StrictRosterRuleAdjuster {
    fun describe(): String
    fun adjust(item: StrictRosterRule, value: Double): Double
}

data class FlaggedStrictRosterRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictRosterRuleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictRosterRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumStrictRosterRuleAdjuster(var minimum: Double) : StrictRosterRuleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictRosterRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingStrictRosterRuleAdjuster(var step: Double) : StrictRosterRuleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictRosterRule, value: Double): Double = roundTo(value, step)
}

data class RegionStrictRosterRuleAdjuster(var factors: MutableMap<String, Double>) : StrictRosterRuleAdjuster {
    override fun describe(): String = "by region"

    override fun adjust(item: StrictRosterRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapStrictRosterRuleAdjuster(var cap: Double) : StrictRosterRuleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictRosterRule, value: Double): Double = minOf(value, cap)
}

data class TieredStrictRosterRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictRosterRuleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictRosterRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict roster rule steps in order and keeps a trace of each. */
data class StrictRosterRuleEngine(
    var steps: MutableList<StrictRosterRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictRosterRule): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictRosterRuleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictRosterRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictRosterRuleEngine(flags: Registry): StrictRosterRuleEngine {
    return StrictRosterRuleEngine(
        steps = mutableListOf<StrictRosterRuleAdjuster>(
            FlaggedStrictRosterRuleAdjuster(flags, "strict-roster-rule-weighted", 4.7),
            MinimumStrictRosterRuleAdjuster(2.6),
            RoundingStrictRosterRuleAdjuster(0.05),
            RegionStrictRosterRuleAdjuster(
                mutableMapOf<String, Double>("retail" to 1.27, "west" to 1.25, "east" to 0.97),
            ),
            CapStrictRosterRuleAdjuster(40303.0),
            TieredStrictRosterRuleAdjuster(
                mutableListOf<Double>(1986.0, 4920.0),
                mutableListOf<Double>(0.93, 0.96),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 95797.4,
    )
}
