package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional attempt rule calculation. */
interface RegionalAttemptRuleAdjuster {
    fun describe(): String
    fun adjust(item: RegionalAttemptRule, value: Double): Double
}

data class FlaggedRegionalAttemptRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalAttemptRuleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalAttemptRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentRegionalAttemptRuleAdjuster(var percent: Double) : RegionalAttemptRuleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalAttemptRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingRegionalAttemptRuleAdjuster(var step: Double) : RegionalAttemptRuleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalAttemptRule, value: Double): Double = roundTo(value, step)
}

data class RegionalAttemptRuleStopCountOffsetAdjuster(var weight: Double) : RegionalAttemptRuleAdjuster {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: RegionalAttemptRule, value: Double): Double = value + item.stopCount.toDouble() * weight
}

/** Runs the regional attempt rule steps in order and keeps a trace of each. */
data class RegionalAttemptRuleEngine(
    var steps: MutableList<RegionalAttemptRuleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalAttemptRule): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalAttemptRuleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalAttemptRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalAttemptRuleEngine(flags: Registry): RegionalAttemptRuleEngine {
    return RegionalAttemptRuleEngine(
        steps = mutableListOf<RegionalAttemptRuleAdjuster>(
            FlaggedRegionalAttemptRuleAdjuster(flags, "regional-attempt-rule-scheduled", -6.5),
            PercentRegionalAttemptRuleAdjuster(-15.1),
            RoundingRegionalAttemptRuleAdjuster(0.1),
            RegionalAttemptRuleStopCountOffsetAdjuster(0.65),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 42580.1,
    )
}
