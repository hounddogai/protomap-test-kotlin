package com.acme.transport.customs.clearance.clearancevariance

import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback clearance variance calculation. */
interface FallbackClearanceVarianceStep {
    fun describe(): String
    fun adjust(item: FallbackClearanceVariance, value: Double): Double
}

data class TieredFallbackClearanceVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackClearanceVarianceStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackClearanceVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingFallbackClearanceVarianceAdjuster(var step: Double) : FallbackClearanceVarianceStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackClearanceVariance, value: Double): Double = roundTo(value, step)
}

data class PercentFallbackClearanceVarianceAdjuster(var percent: Double) : FallbackClearanceVarianceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackClearanceVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapFallbackClearanceVarianceAdjuster(var cap: Double) : FallbackClearanceVarianceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackClearanceVariance, value: Double): Double = minOf(value, cap)
}

data class FallbackClearanceVarianceQuantityOffsetAdjuster(var weight: Double) : FallbackClearanceVarianceStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: FallbackClearanceVariance, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class DepotFallbackClearanceVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackClearanceVarianceStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: FallbackClearanceVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fallback clearance variance steps in order and keeps a trace of each. */
data class FallbackClearanceVarianceEngine(
    var steps: MutableList<FallbackClearanceVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackClearanceVariance): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackClearanceVarianceStep = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackClearanceVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackClearanceVarianceEngine(): FallbackClearanceVarianceEngine {
    return FallbackClearanceVarianceEngine(
        steps = mutableListOf<FallbackClearanceVarianceStep>(
            TieredFallbackClearanceVarianceAdjuster(
                mutableListOf<Double>(990.0, 6559.0, 6914.0),
                mutableListOf<Double>(0.98, 1.1, 0.97),
            ),
            RoundingFallbackClearanceVarianceAdjuster(1.0),
            PercentFallbackClearanceVarianceAdjuster(5.6),
            CapFallbackClearanceVarianceAdjuster(3262.0),
            FallbackClearanceVarianceQuantityOffsetAdjuster(1.4),
            DepotFallbackClearanceVarianceAdjuster(
                mutableMapOf<String, Double>("online" to 0.89, "south" to 1.39, "retail" to 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 74990.1,
    )
}
