package com.acme.transport.customs.tariff.tariffpolicy

import com.acme.transport.kit.numbers.roundTo

/** One step of the regional tariff rollup calculation. */
interface RegionalTariffRollupModifier {
    fun describe(): String
    fun adjust(item: RegionalTariffRollup, value: Double): Double
}

data class RoundingRegionalTariffRollupAdjuster(var step: Double) : RegionalTariffRollupModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalTariffRollup, value: Double): Double = roundTo(value, step)
}

data class PercentRegionalTariffRollupAdjuster(var percent: Double) : RegionalTariffRollupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalTariffRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumRegionalTariffRollupAdjuster(var minimum: Double) : RegionalTariffRollupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalTariffRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRegionalTariffRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalTariffRollupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalTariffRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the regional tariff rollup steps in order and keeps a trace of each. */
data class RegionalTariffRollupEngine(
    var steps: MutableList<RegionalTariffRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalTariffRollup): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalTariffRollupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalTariffRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalTariffRollupEngine(): RegionalTariffRollupEngine {
    return RegionalTariffRollupEngine(
        steps = mutableListOf<RegionalTariffRollupModifier>(
            RoundingRegionalTariffRollupAdjuster(1.0),
            PercentRegionalTariffRollupAdjuster(-15.5),
            MinimumRegionalTariffRollupAdjuster(28.7),
            TieredRegionalTariffRollupAdjuster(
                mutableListOf<Double>(378.0, 7939.0),
                mutableListOf<Double>(0.99, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 6651.4,
    )
}
