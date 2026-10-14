package com.acme.transport.linehaul.laneplan.item

/** One step of the capped lane plan preference calculation. */
interface CappedLanePlanPreferenceFactor {
    fun describe(): String
    fun adjust(item: CappedLanePlanPreference, value: Double): Double
}

data class MinimumCappedLanePlanPreferenceAdjuster(var minimum: Double) : CappedLanePlanPreferenceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedLanePlanPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCappedLanePlanPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedLanePlanPreferenceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedLanePlanPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCappedLanePlanPreferenceAdjuster(var percent: Double) : CappedLanePlanPreferenceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedLanePlanPreference, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CappedLanePlanPreferenceCeilingOffsetAdjuster(var weight: Double) : CappedLanePlanPreferenceFactor {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: CappedLanePlanPreference, value: Double): Double = value + item.ceiling * weight
}

/** Runs the capped lane plan preference steps in order and keeps a trace of each. */
data class CappedLanePlanPreferenceEngine(
    var steps: MutableList<CappedLanePlanPreferenceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedLanePlanPreference): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedLanePlanPreferenceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CappedLanePlanPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedLanePlanPreferenceEngine(): CappedLanePlanPreferenceEngine {
    return CappedLanePlanPreferenceEngine(
        steps = mutableListOf<CappedLanePlanPreferenceFactor>(
            MinimumCappedLanePlanPreferenceAdjuster(18.8),
            TieredCappedLanePlanPreferenceAdjuster(
                mutableListOf<Double>(1939.0, 5337.0, 7935.0),
                mutableListOf<Double>(0.97, 1.03, 1.07),
            ),
            PercentCappedLanePlanPreferenceAdjuster(-19.1),
            CappedLanePlanPreferenceCeilingOffsetAdjuster(1.65),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 78624.1,
    )
}
