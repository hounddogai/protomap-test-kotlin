package com.acme.transport.crewing.availability.scheduledavailabilitytier

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual availability rule calculation. */
interface ManualAvailabilityRuleFactor {
    fun describe(): String
    fun adjust(item: ManualAvailabilityRule, value: Double): Double
}

data class MinimumManualAvailabilityRuleAdjuster(var minimum: Double) : ManualAvailabilityRuleFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualAvailabilityRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredManualAvailabilityRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualAvailabilityRuleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualAvailabilityRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapManualAvailabilityRuleAdjuster(var cap: Double) : ManualAvailabilityRuleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualAvailabilityRule, value: Double): Double = minOf(value, cap)
}

data class TitleManualAvailabilityRuleAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualAvailabilityRuleFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: ManualAvailabilityRule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual availability rule steps in order and keeps a trace of each. */
data class ManualAvailabilityRuleEngine(
    var steps: MutableList<ManualAvailabilityRuleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualAvailabilityRule): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualAvailabilityRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualAvailabilityRuleEngine(): ManualAvailabilityRuleEngine {
    return ManualAvailabilityRuleEngine(
        steps = mutableListOf<ManualAvailabilityRuleFactor>(
            MinimumManualAvailabilityRuleAdjuster(45.8),
            TieredManualAvailabilityRuleAdjuster(
                mutableListOf<Double>(6589.0, 9620.0),
                mutableListOf<Double>(0.93, 0.86),
            ),
            CapManualAvailabilityRuleAdjuster(39861.0),
            TitleManualAvailabilityRuleAdjuster(
                mutableMapOf<String, Double>("south" to 0.97, "west" to 0.71, "retail" to 0.91),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 17692.7,
    )
}
