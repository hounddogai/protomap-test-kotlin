package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

/** One step of the seasonal emission factor exception calculation. */
interface SeasonalEmissionFactorExceptionModifier {
    fun describe(): String
    fun adjust(item: SeasonalEmissionFactorException, value: Double): Double
}

data class MinimumSeasonalEmissionFactorExceptionAdjuster(
    var minimum: Double,
) : SeasonalEmissionFactorExceptionModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalEmissionFactorException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelSeasonalEmissionFactorExceptionAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalEmissionFactorExceptionModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: SeasonalEmissionFactorException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredSeasonalEmissionFactorExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalEmissionFactorExceptionModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalEmissionFactorException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapSeasonalEmissionFactorExceptionAdjuster(
    var cap: Double,
) : SeasonalEmissionFactorExceptionModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalEmissionFactorException, value: Double): Double = minOf(value, cap)
}

/** Runs the seasonal emission factor exception steps in order and keeps a trace of each. */
data class SeasonalEmissionFactorExceptionEngine(
    var steps: MutableList<SeasonalEmissionFactorExceptionModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalEmissionFactorException): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalEmissionFactorExceptionModifier = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalEmissionFactorException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalEmissionFactorExceptionEngine(): SeasonalEmissionFactorExceptionEngine {
    return SeasonalEmissionFactorExceptionEngine(
        steps = mutableListOf<SeasonalEmissionFactorExceptionModifier>(
            MinimumSeasonalEmissionFactorExceptionAdjuster(21.4),
            LabelSeasonalEmissionFactorExceptionAdjuster(
                mutableMapOf<String, Double>("central" to 1.38, "west" to 0.9),
            ),
            TieredSeasonalEmissionFactorExceptionAdjuster(
                mutableListOf<Double>(1702.0, 8701.0, 8714.0),
                mutableListOf<Double>(1.07, 0.96, 1.16),
            ),
            CapSeasonalEmissionFactorExceptionAdjuster(4351.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 48660.1,
    )
}
