package com.acme.transport.emissions.evcharge.preference

/** One step of the lenient ev charge preference calculation. */
interface LenientEvChargePreferenceStep {
    fun describe(): String
    fun adjust(item: LenientEvChargePreference, value: Double): Double
}

data class LabelLenientEvChargePreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientEvChargePreferenceStep {
    override fun describe(): String = "by label"

    override fun adjust(item: LenientEvChargePreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLenientEvChargePreferenceAdjuster(var percent: Double) : LenientEvChargePreferenceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientEvChargePreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLenientEvChargePreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientEvChargePreferenceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientEvChargePreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lenient ev charge preference steps in order and keeps a trace of each. */
data class LenientEvChargePreferenceEngine(
    var steps: MutableList<LenientEvChargePreferenceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LenientEvChargePreference): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientEvChargePreferenceStep = steps[index]
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

    fun evaluateAll(items: MutableList<LenientEvChargePreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLenientEvChargePreferenceEngine(): LenientEvChargePreferenceEngine {
    return LenientEvChargePreferenceEngine(
        steps = mutableListOf<LenientEvChargePreferenceStep>(
            LabelLenientEvChargePreferenceAdjuster(
                mutableMapOf<String, Double>("east" to 0.95, "retail" to 1.21, "north" to 1.09),
            ),
            PercentLenientEvChargePreferenceAdjuster(-4.0),
            TieredLenientEvChargePreferenceAdjuster(
                mutableListOf<Double>(2635.0, 3939.0, 6632.0, 9230.0),
                mutableListOf<Double>(0.91, 0.8, 0.92, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 61718.2,
    )
}
