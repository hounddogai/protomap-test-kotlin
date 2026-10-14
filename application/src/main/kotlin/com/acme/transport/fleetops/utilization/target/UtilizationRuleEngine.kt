package com.acme.transport.fleetops.utilization.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the utilization rule calculation. */
interface UtilizationRuleModifier {
    fun describe(): String
    fun adjust(item: UtilizationRule, value: Double): Double
}

data class FlaggedUtilizationRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : UtilizationRuleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: UtilizationRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingUtilizationRuleAdjuster(var step: Double) : UtilizationRuleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: UtilizationRule, value: Double): Double = roundTo(value, step)
}

data class TieredUtilizationRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : UtilizationRuleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: UtilizationRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the utilization rule steps in order and keeps a trace of each. */
data class UtilizationRuleEngine(
    var steps: MutableList<UtilizationRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: UtilizationRule): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<UtilizationRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultUtilizationRuleEngine(flags: Registry): UtilizationRuleEngine {
    return UtilizationRuleEngine(
        steps = mutableListOf<UtilizationRuleModifier>(
            FlaggedUtilizationRuleAdjuster(flags, "utilization-rule-seasonal", -2.2),
            RoundingUtilizationRuleAdjuster(5.0),
            TieredUtilizationRuleAdjuster(
                mutableListOf<Double>(1300.0, 8108.0, 9626.0),
                mutableListOf<Double>(1.04, 1.18, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 13293.4,
    )
}
