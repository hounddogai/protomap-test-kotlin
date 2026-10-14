package com.acme.transport.linehaul.tender.metric

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual tender rule calculation. */
interface ManualTenderRuleModifier {
    fun describe(): String
    fun adjust(item: ManualTenderRule, value: Double): Double
}

data class CapManualTenderRuleAdjuster(var cap: Double) : ManualTenderRuleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualTenderRule, value: Double): Double = minOf(value, cap)
}

data class TieredManualTenderRuleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualTenderRuleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualTenderRule, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumManualTenderRuleAdjuster(var minimum: Double) : ManualTenderRuleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualTenderRule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ManualTenderRuleWeightOffsetAdjuster(var weight: Double) : ManualTenderRuleModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: ManualTenderRule, value: Double): Double = value + item.weight * weight
}

/** Runs the manual tender rule steps in order and keeps a trace of each. */
data class ManualTenderRuleEngine(
    var steps: MutableList<ManualTenderRuleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualTenderRule): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualTenderRule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualTenderRuleEngine(): ManualTenderRuleEngine {
    return ManualTenderRuleEngine(
        steps = mutableListOf<ManualTenderRuleModifier>(
            CapManualTenderRuleAdjuster(20015.0),
            TieredManualTenderRuleAdjuster(
                mutableListOf<Double>(4660.0, 5381.0, 9362.0),
                mutableListOf<Double>(1.16, 0.88, 0.98),
            ),
            MinimumManualTenderRuleAdjuster(20.3),
            ManualTenderRuleWeightOffsetAdjuster(1.68),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 38341.9,
    )
}
