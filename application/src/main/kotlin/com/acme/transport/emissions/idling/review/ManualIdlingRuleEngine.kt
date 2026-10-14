package com.acme.transport.emissions.idling.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual idling rule calculation. */
interface ManualIdlingRuleStep {
    fun describe(): String
    fun adjust(item: ManualIdlingRule, value: Double): Double
}

data class FlaggedManualIdlingRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualIdlingRuleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualIdlingRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentManualIdlingRuleAdjuster(var percent: Double) : ManualIdlingRuleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualIdlingRule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ManualIdlingRuleRateOffsetAdjuster(var weight: Double) : ManualIdlingRuleStep {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: ManualIdlingRule, value: Double): Double = value + item.rate * weight
}

data class CapManualIdlingRuleAdjuster(var cap: Double) : ManualIdlingRuleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualIdlingRule, value: Double): Double = minOf(value, cap)
}

data class MinimumManualIdlingRuleAdjuster(var minimum: Double) : ManualIdlingRuleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualIdlingRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual idling rule steps in order and keeps a trace of each. */
data class ManualIdlingRuleEngine(
    var steps: MutableList<ManualIdlingRuleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualIdlingRule): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualIdlingRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualIdlingRuleEngine(flags: Registry): ManualIdlingRuleEngine {
    return ManualIdlingRuleEngine(
        steps = mutableListOf<ManualIdlingRuleStep>(
            FlaggedManualIdlingRuleAdjuster(flags, "manual-idling-rule-weighted", -9.4),
            PercentManualIdlingRuleAdjuster(12.9),
            ManualIdlingRuleRateOffsetAdjuster(1.53),
            CapManualIdlingRuleAdjuster(17317.0),
            MinimumManualIdlingRuleAdjuster(10.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 97106.7,
    )
}
