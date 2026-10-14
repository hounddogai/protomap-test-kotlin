package com.acme.transport.linehaul.tender.variance

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual tender variance calculation. */
interface ManualTenderVarianceStep {
    fun describe(): String
    fun adjust(item: ManualTenderVariance, value: Double): Double
}

data class TieredManualTenderVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualTenderVarianceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualTenderVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentManualTenderVarianceAdjuster(var percent: Double) : ManualTenderVarianceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualTenderVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumManualTenderVarianceAdjuster(var minimum: Double) : ManualTenderVarianceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualTenderVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ManualTenderVarianceDistanceKmOffsetAdjuster(var weight: Double) : ManualTenderVarianceStep {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: ManualTenderVariance, value: Double): Double = value + item.distanceKm * weight
}

/** Runs the manual tender variance steps in order and keeps a trace of each. */
data class ManualTenderVarianceEngine(
    var steps: MutableList<ManualTenderVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualTenderVariance): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualTenderVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualTenderVarianceEngine(): ManualTenderVarianceEngine {
    return ManualTenderVarianceEngine(
        steps = mutableListOf<ManualTenderVarianceStep>(
            TieredManualTenderVarianceAdjuster(
                mutableListOf<Double>(4389.0, 7181.0),
                mutableListOf<Double>(0.96, 1.11),
            ),
            PercentManualTenderVarianceAdjuster(-6.0),
            MinimumManualTenderVarianceAdjuster(34.8),
            ManualTenderVarianceDistanceKmOffsetAdjuster(1.42),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 77236.3,
    )
}
