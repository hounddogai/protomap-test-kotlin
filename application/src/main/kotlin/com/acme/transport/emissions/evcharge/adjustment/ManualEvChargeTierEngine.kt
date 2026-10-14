package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual ev charge tier calculation. */
interface ManualEvChargeTierModifier {
    fun describe(): String
    fun adjust(item: ManualEvChargeTier, value: Double): Double
}

data class RegionManualEvChargeTierAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualEvChargeTierModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: ManualEvChargeTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredManualEvChargeTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualEvChargeTierModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualEvChargeTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingManualEvChargeTierAdjuster(var step: Double) : ManualEvChargeTierModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualEvChargeTier, value: Double): Double = roundTo(value, step)
}

data class PercentManualEvChargeTierAdjuster(var percent: Double) : ManualEvChargeTierModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualEvChargeTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the manual ev charge tier steps in order and keeps a trace of each. */
data class ManualEvChargeTierEngine(
    var steps: MutableList<ManualEvChargeTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualEvChargeTier): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualEvChargeTierModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ManualEvChargeTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualEvChargeTierEngine(): ManualEvChargeTierEngine {
    return ManualEvChargeTierEngine(
        steps = mutableListOf<ManualEvChargeTierModifier>(
            RegionManualEvChargeTierAdjuster(mutableMapOf<String, Double>("north" to 0.76)),
            TieredManualEvChargeTierAdjuster(
                mutableListOf<Double>(2464.0, 3349.0, 6661.0, 8723.0),
                mutableListOf<Double>(1.16, 1.13, 0.92, 0.97),
            ),
            RoundingManualEvChargeTierAdjuster(5.0),
            PercentManualEvChargeTierAdjuster(13.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 89825.4,
    )
}
