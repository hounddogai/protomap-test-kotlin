package com.acme.transport.freightpay.accessorial.variance

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted accessorial variance calculation. */
interface WeightedAccessorialVarianceAdjuster {
    fun describe(): String
    fun adjust(item: WeightedAccessorialVariance, value: Double): Double
}

data class WeightedAccessorialVarianceAmountOffsetAdjuster(
    var weight: Double,
) : WeightedAccessorialVarianceAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: WeightedAccessorialVariance, value: Double): Double = value + item.amount * weight
}

data class MinimumWeightedAccessorialVarianceAdjuster(
    var minimum: Double,
) : WeightedAccessorialVarianceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedAccessorialVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingWeightedAccessorialVarianceAdjuster(var step: Double) : WeightedAccessorialVarianceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedAccessorialVariance, value: Double): Double = roundTo(value, step)
}

data class CapWeightedAccessorialVarianceAdjuster(var cap: Double) : WeightedAccessorialVarianceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedAccessorialVariance, value: Double): Double = minOf(value, cap)
}

data class SourceWeightedAccessorialVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedAccessorialVarianceAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: WeightedAccessorialVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentWeightedAccessorialVarianceAdjuster(
    var percent: Double,
) : WeightedAccessorialVarianceAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedAccessorialVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the weighted accessorial variance steps in order and keeps a trace of each. */
data class WeightedAccessorialVarianceEngine(
    var steps: MutableList<WeightedAccessorialVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedAccessorialVariance): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedAccessorialVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedAccessorialVarianceEngine(): WeightedAccessorialVarianceEngine {
    return WeightedAccessorialVarianceEngine(
        steps = mutableListOf<WeightedAccessorialVarianceAdjuster>(
            WeightedAccessorialVarianceAmountOffsetAdjuster(1.45),
            MinimumWeightedAccessorialVarianceAdjuster(10.1),
            RoundingWeightedAccessorialVarianceAdjuster(0.05),
            CapWeightedAccessorialVarianceAdjuster(12758.0),
            SourceWeightedAccessorialVarianceAdjuster(mutableMapOf<String, Double>("east" to 1.05)),
            PercentWeightedAccessorialVarianceAdjuster(-7.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 64482.3,
    )
}
