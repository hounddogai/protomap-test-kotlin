package com.acme.transport.terminal.dwell.rule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped dwell rule calculation. */
interface CappedDwellRuleModifier {
    fun describe(): String
    fun adjust(item: CappedDwellRule, value: Double): Double
}

data class TieredCappedDwellRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedDwellRuleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedDwellRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCappedDwellRuleAdjuster(var minimum: Double) : CappedDwellRuleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedDwellRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCappedDwellRuleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedDwellRuleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedDwellRule, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCappedDwellRuleAdjuster(var cap: Double) : CappedDwellRuleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedDwellRule, value: Double): Double = minOf(value, cap)
}

data class UnitCappedDwellRuleAdjuster(var factors: MutableMap<String, Double>) : CappedDwellRuleModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: CappedDwellRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CappedDwellRuleRevisionOffsetAdjuster(var weight: Double) : CappedDwellRuleModifier {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: CappedDwellRule, value: Double): Double = value + item.revision.toDouble() * weight
}

/** Runs the capped dwell rule steps in order and keeps a trace of each. */
data class CappedDwellRuleEngine(
    var steps: MutableList<CappedDwellRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedDwellRule): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedDwellRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedDwellRuleEngine(flags: Registry): CappedDwellRuleEngine {
    return CappedDwellRuleEngine(
        steps = mutableListOf<CappedDwellRuleModifier>(
            TieredCappedDwellRuleAdjuster(
                mutableListOf<Double>(5630.0, 6070.0, 8450.0, 9727.0),
                mutableListOf<Double>(1.1, 1.2, 0.87, 0.93),
            ),
            MinimumCappedDwellRuleAdjuster(35.7),
            FlaggedCappedDwellRuleAdjuster(flags, "capped-dwell-rule-seasonal", 8.9),
            CapCappedDwellRuleAdjuster(29060.0),
            UnitCappedDwellRuleAdjuster(
                mutableMapOf<String, Double>("east" to 0.89, "north" to 0.81, "central" to 1.07),
            ),
            CappedDwellRuleRevisionOffsetAdjuster(1.54),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 38634.3,
    )
}
