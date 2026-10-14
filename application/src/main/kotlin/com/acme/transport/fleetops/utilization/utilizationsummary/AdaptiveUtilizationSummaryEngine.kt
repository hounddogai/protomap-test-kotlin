package com.acme.transport.fleetops.utilization.utilizationsummary

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive utilization summary calculation. */
interface AdaptiveUtilizationSummaryAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveUtilizationSummary, value: Double): Double
}

data class AdaptiveUtilizationSummaryFloorValueOffsetAdjuster(
    var weight: Double,
) : AdaptiveUtilizationSummaryAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: AdaptiveUtilizationSummary, value: Double): Double = value + item.floorValue * weight
}

data class CapAdaptiveUtilizationSummaryAdjuster(var cap: Double) : AdaptiveUtilizationSummaryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveUtilizationSummary, value: Double): Double = minOf(value, cap)
}

data class TieredAdaptiveUtilizationSummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveUtilizationSummaryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveUtilizationSummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive utilization summary steps in order and keeps a trace of each. */
data class AdaptiveUtilizationSummaryEngine(
    var steps: MutableList<AdaptiveUtilizationSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveUtilizationSummary): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveUtilizationSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveUtilizationSummaryEngine(): AdaptiveUtilizationSummaryEngine {
    return AdaptiveUtilizationSummaryEngine(
        steps = mutableListOf<AdaptiveUtilizationSummaryAdjuster>(
            AdaptiveUtilizationSummaryFloorValueOffsetAdjuster(1.14),
            CapAdaptiveUtilizationSummaryAdjuster(43576.0),
            TieredAdaptiveUtilizationSummaryAdjuster(
                mutableListOf<Double>(4413.0, 4655.0, 8293.0),
                mutableListOf<Double>(1.19, 1.05, 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 84761.4,
    )
}
