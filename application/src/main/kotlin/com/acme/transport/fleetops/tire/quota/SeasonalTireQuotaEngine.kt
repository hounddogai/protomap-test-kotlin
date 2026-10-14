package com.acme.transport.fleetops.tire.quota

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal tire quota calculation. */
interface SeasonalTireQuotaAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalTireQuota, value: Double): Double
}

data class SeasonalTireQuotaOdometerOffsetAdjuster(var weight: Double) : SeasonalTireQuotaAdjuster {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: SeasonalTireQuota, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class PercentSeasonalTireQuotaAdjuster(var percent: Double) : SeasonalTireQuotaAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalTireQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredSeasonalTireQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalTireQuotaAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalTireQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the seasonal tire quota steps in order and keeps a trace of each. */
data class SeasonalTireQuotaEngine(
    var steps: MutableList<SeasonalTireQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: SeasonalTireQuota): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalTireQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultSeasonalTireQuotaEngine(): SeasonalTireQuotaEngine {
    return SeasonalTireQuotaEngine(
        steps = mutableListOf<SeasonalTireQuotaAdjuster>(
            SeasonalTireQuotaOdometerOffsetAdjuster(1.91),
            PercentSeasonalTireQuotaAdjuster(-21.1),
            TieredSeasonalTireQuotaAdjuster(
                mutableListOf<Double>(542.0, 793.0, 7548.0),
                mutableListOf<Double>(0.83, 0.83, 1.07),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 76532.3,
    )
}
