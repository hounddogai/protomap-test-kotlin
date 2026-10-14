package com.acme.transport.linehaul.relay.weightedrelayrule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient relay rule calculation. */
interface LenientRelayRuleFactor {
    fun describe(): String
    fun adjust(item: LenientRelayRule, value: Double): Double
}

data class PercentLenientRelayRuleAdjuster(var percent: Double) : LenientRelayRuleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientRelayRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingLenientRelayRuleAdjuster(var step: Double) : LenientRelayRuleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientRelayRule, value: Double): Double = roundTo(value, step)
}

data class LenientRelayRuleOdometerOffsetAdjuster(var weight: Double) : LenientRelayRuleFactor {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: LenientRelayRule, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class TieredLenientRelayRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientRelayRuleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientRelayRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CountryLenientRelayRuleAdjuster(var factors: MutableMap<String, Double>) : LenientRelayRuleFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: LenientRelayRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLenientRelayRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientRelayRuleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientRelayRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lenient relay rule steps in order and keeps a trace of each. */
data class LenientRelayRuleEngine(
    var steps: MutableList<LenientRelayRuleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientRelayRule): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientRelayRuleFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LenientRelayRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientRelayRuleEngine(flags: Registry): LenientRelayRuleEngine {
    return LenientRelayRuleEngine(
        steps = mutableListOf<LenientRelayRuleFactor>(
            PercentLenientRelayRuleAdjuster(-21.3),
            RoundingLenientRelayRuleAdjuster(1.0),
            LenientRelayRuleOdometerOffsetAdjuster(1.12),
            TieredLenientRelayRuleAdjuster(
                mutableListOf<Double>(4130.0, 4635.0, 6831.0, 8323.0),
                mutableListOf<Double>(1.11, 1.12, 0.98, 1.05),
            ),
            CountryLenientRelayRuleAdjuster(mutableMapOf<String, Double>("online" to 1.34)),
            FlaggedLenientRelayRuleAdjuster(flags, "lenient-relay-rule-seasonal", 6.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 48654.7,
    )
}
