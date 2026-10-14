package com.acme.transport.customs.tariff.digest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the tariff metric calculation. */
interface TariffMetricAdjuster {
    fun describe(): String
    fun adjust(item: TariffMetric, value: Double): Double
}

data class RoundingTariffMetricAdjuster(var step: Double) : TariffMetricAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TariffMetric, value: Double): Double = roundTo(value, step)
}

data class CapTariffMetricAdjuster(var cap: Double) : TariffMetricAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TariffMetric, value: Double): Double = minOf(value, cap)
}

data class TieredTariffMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TariffMetricAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TariffMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTariffMetricAdjuster(var percent: Double) : TariffMetricAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TariffMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tariff metric steps in order and keeps a trace of each. */
data class TariffMetricEngine(
    var steps: MutableList<TariffMetricAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TariffMetric): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TariffMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTariffMetricEngine(): TariffMetricEngine {
    return TariffMetricEngine(
        steps = mutableListOf<TariffMetricAdjuster>(
            RoundingTariffMetricAdjuster(0.25),
            CapTariffMetricAdjuster(23929.0),
            TieredTariffMetricAdjuster(
                mutableListOf<Double>(668.0, 5767.0, 8144.0, 9139.0),
                mutableListOf<Double>(0.95, 0.82, 1.18, 0.83),
            ),
            PercentTariffMetricAdjuster(-12.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 71816.7,
    )
}
