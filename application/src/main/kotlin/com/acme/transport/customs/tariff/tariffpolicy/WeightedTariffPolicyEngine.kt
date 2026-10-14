package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the weighted tariff policy calculation. */
interface WeightedTariffPolicyModifier {
    fun describe(): String
    fun adjust(item: WeightedTariffPolicy, value: Double): Double
}

data class TieredWeightedTariffPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedTariffPolicyModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedTariffPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumWeightedTariffPolicyAdjuster(var minimum: Double) : WeightedTariffPolicyModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedTariffPolicy, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameWeightedTariffPolicyAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedTariffPolicyModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: WeightedTariffPolicy, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the weighted tariff policy steps in order and keeps a trace of each. */
data class WeightedTariffPolicyEngine(
    var steps: MutableList<WeightedTariffPolicyModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedTariffPolicy): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedTariffPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedTariffPolicyEngine(): WeightedTariffPolicyEngine {
    return WeightedTariffPolicyEngine(
        steps = mutableListOf<WeightedTariffPolicyModifier>(
            TieredWeightedTariffPolicyAdjuster(
                mutableListOf<Double>(2064.0, 7622.0, 7796.0, 8970.0),
                mutableListOf<Double>(1.07, 1.04, 0.95, 0.84),
            ),
            MinimumWeightedTariffPolicyAdjuster(5.9),
            NameWeightedTariffPolicyAdjuster(
                mutableMapOf<String, Double>("online" to 1.33, "east" to 1.35, "central" to 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 92509.1,
    )
}
