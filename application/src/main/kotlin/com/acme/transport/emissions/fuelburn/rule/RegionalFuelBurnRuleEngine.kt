package com.acme.transport.emissions.fuelburn.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional fuel burn rule calculation. */
interface RegionalFuelBurnRuleModifier {
    fun describe(): String
    fun adjust(item: RegionalFuelBurnRule, value: Double): Double
}

data class RegionalFuelBurnRuleWindowDaysOffsetAdjuster(var weight: Double) : RegionalFuelBurnRuleModifier {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: RegionalFuelBurnRule, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class MinimumRegionalFuelBurnRuleAdjuster(var minimum: Double) : RegionalFuelBurnRuleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalFuelBurnRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedRegionalFuelBurnRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalFuelBurnRuleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalFuelBurnRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional fuel burn rule steps in order and keeps a trace of each. */
data class RegionalFuelBurnRuleEngine(
    var steps: MutableList<RegionalFuelBurnRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalFuelBurnRule): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalFuelBurnRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalFuelBurnRuleEngine(flags: Registry): RegionalFuelBurnRuleEngine {
    return RegionalFuelBurnRuleEngine(
        steps = mutableListOf<RegionalFuelBurnRuleModifier>(
            RegionalFuelBurnRuleWindowDaysOffsetAdjuster(1.18),
            MinimumRegionalFuelBurnRuleAdjuster(40.3),
            FlaggedRegionalFuelBurnRuleAdjuster(flags, "regional-fuel-burn-rule-capped", -9.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 4346.3,
    )
}
