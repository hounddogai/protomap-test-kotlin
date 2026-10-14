package com.acme.transport.customs.tariff.draft

import com.acme.transport.kit.numbers.roundTo

/** One step of the tariff rollup calculation. */
interface TariffRollupAdjuster {
    fun describe(): String
    fun adjust(item: TariffRollup, value: Double): Double
}

data class PercentTariffRollupAdjuster(var percent: Double) : TariffRollupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TariffRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTariffRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TariffRollupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TariffRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTariffRollupAdjuster(var step: Double) : TariffRollupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TariffRollup, value: Double): Double = roundTo(value, step)
}

data class CapTariffRollupAdjuster(var cap: Double) : TariffRollupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TariffRollup, value: Double): Double = minOf(value, cap)
}

/** Runs the tariff rollup steps in order and keeps a trace of each. */
data class TariffRollupEngine(
    var steps: MutableList<TariffRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TariffRollup): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TariffRollupAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<TariffRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTariffRollupEngine(): TariffRollupEngine {
    return TariffRollupEngine(
        steps = mutableListOf<TariffRollupAdjuster>(
            PercentTariffRollupAdjuster(14.2),
            TieredTariffRollupAdjuster(
                mutableListOf<Double>(824.0, 876.0, 991.0, 3490.0),
                mutableListOf<Double>(0.87, 0.86, 1.15, 0.91),
            ),
            RoundingTariffRollupAdjuster(0.05),
            CapTariffRollupAdjuster(22636.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 11847.1,
    )
}
