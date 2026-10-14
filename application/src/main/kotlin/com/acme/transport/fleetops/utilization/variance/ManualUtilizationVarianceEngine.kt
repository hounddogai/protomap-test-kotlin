package com.acme.transport.fleetops.utilization.variance

import com.acme.transport.kit.numbers.roundTo

/** One step of the manual utilization variance calculation. */
interface ManualUtilizationVarianceFactor {
    fun describe(): String
    fun adjust(item: ManualUtilizationVariance, value: Double): Double
}

data class RegionManualUtilizationVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualUtilizationVarianceFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: ManualUtilizationVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapManualUtilizationVarianceAdjuster(var cap: Double) : ManualUtilizationVarianceFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualUtilizationVariance, value: Double): Double = minOf(value, cap)
}

data class RoundingManualUtilizationVarianceAdjuster(var step: Double) : ManualUtilizationVarianceFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualUtilizationVariance, value: Double): Double = roundTo(value, step)
}

data class ManualUtilizationVarianceExpiresAtOffsetAdjuster(
    var weight: Double,
) : ManualUtilizationVarianceFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: ManualUtilizationVariance, value: Double): Double {
        return value + item.expiresAt.toDouble() * weight
    }
}

data class PercentManualUtilizationVarianceAdjuster(var percent: Double) : ManualUtilizationVarianceFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualUtilizationVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumManualUtilizationVarianceAdjuster(var minimum: Double) : ManualUtilizationVarianceFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualUtilizationVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual utilization variance steps in order and keeps a trace of each. */
data class ManualUtilizationVarianceEngine(
    var steps: MutableList<ManualUtilizationVarianceFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ManualUtilizationVariance): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualUtilizationVarianceFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ManualUtilizationVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultManualUtilizationVarianceEngine(): ManualUtilizationVarianceEngine {
    return ManualUtilizationVarianceEngine(
        steps = mutableListOf<ManualUtilizationVarianceFactor>(
            RegionManualUtilizationVarianceAdjuster(
                mutableMapOf<String, Double>("west" to 0.97, "retail" to 0.73),
            ),
            CapManualUtilizationVarianceAdjuster(46603.0),
            RoundingManualUtilizationVarianceAdjuster(5.0),
            ManualUtilizationVarianceExpiresAtOffsetAdjuster(0.87),
            PercentManualUtilizationVarianceAdjuster(18.3),
            MinimumManualUtilizationVarianceAdjuster(21.1),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 26905.8,
    )
}
