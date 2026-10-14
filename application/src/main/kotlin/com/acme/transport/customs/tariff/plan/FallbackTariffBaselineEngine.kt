package com.acme.transport.customs.tariff.plan

/** One step of the fallback tariff baseline calculation. */
interface FallbackTariffBaselineFactor {
    fun describe(): String
    fun adjust(item: FallbackTariffBaseline, value: Double): Double
}

data class FallbackTariffBaselineMaximumOffsetAdjuster(var weight: Double) : FallbackTariffBaselineFactor {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: FallbackTariffBaseline, value: Double): Double = value + item.maximum * weight
}

data class MinimumFallbackTariffBaselineAdjuster(var minimum: Double) : FallbackTariffBaselineFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackTariffBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentFallbackTariffBaselineAdjuster(var percent: Double) : FallbackTariffBaselineFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackTariffBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredFallbackTariffBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackTariffBaselineFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackTariffBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the fallback tariff baseline steps in order and keeps a trace of each. */
data class FallbackTariffBaselineEngine(
    var steps: MutableList<FallbackTariffBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackTariffBaseline): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackTariffBaselineFactor = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackTariffBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackTariffBaselineEngine(): FallbackTariffBaselineEngine {
    return FallbackTariffBaselineEngine(
        steps = mutableListOf<FallbackTariffBaselineFactor>(
            FallbackTariffBaselineMaximumOffsetAdjuster(0.58),
            MinimumFallbackTariffBaselineAdjuster(42.4),
            PercentFallbackTariffBaselineAdjuster(-18.7),
            TieredFallbackTariffBaselineAdjuster(
                mutableListOf<Double>(5570.0, 8100.0, 8784.0, 9336.0),
                mutableListOf<Double>(0.8, 0.94, 1.04, 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 63469.4,
    )
}
