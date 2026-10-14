package com.acme.transport.customs.tariff.budget

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global tariff variance calculation. */
interface GlobalTariffVarianceFactor {
    fun describe(): String
    fun adjust(item: GlobalTariffVariance, value: Double): Double
}

data class GlobalTariffVariancePalletsOffsetAdjuster(var weight: Double) : GlobalTariffVarianceFactor {
    override fun describe(): String = "plus pallets times $weight"

    override fun adjust(item: GlobalTariffVariance, value: Double): Double = value + item.pallets.toDouble() * weight
}

data class LabelGlobalTariffVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalTariffVarianceFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: GlobalTariffVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingGlobalTariffVarianceAdjuster(var step: Double) : GlobalTariffVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalTariffVariance, value: Double): Double = roundTo(value, step)
}

data class CapGlobalTariffVarianceAdjuster(var cap: Double) : GlobalTariffVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalTariffVariance, value: Double): Double = minOf(value, cap)
}

data class TieredGlobalTariffVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalTariffVarianceFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalTariffVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the global tariff variance steps in order and keeps a trace of each. */
data class GlobalTariffVarianceEngine(
    var steps: MutableList<GlobalTariffVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalTariffVariance): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalTariffVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalTariffVarianceEngine(): GlobalTariffVarianceEngine {
    return GlobalTariffVarianceEngine(
        steps = mutableListOf<GlobalTariffVarianceFactor>(
            GlobalTariffVariancePalletsOffsetAdjuster(1.81),
            LabelGlobalTariffVarianceAdjuster(mutableMapOf<String, Double>("south" to 0.93)),
            RoundingGlobalTariffVarianceAdjuster(5.0),
            CapGlobalTariffVarianceAdjuster(4750.0),
            TieredGlobalTariffVarianceAdjuster(
                mutableListOf<Double>(1211.0, 3126.0),
                mutableListOf<Double>(0.87, 0.95),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 5841.7,
    )
}
