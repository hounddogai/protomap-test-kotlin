package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.kit.numbers.roundTo

/** One step of the lane plan variance calculation. */
interface LanePlanVarianceAdjuster {
    fun describe(): String
    fun adjust(item: LanePlanVariance, value: Double): Double
}

data class TieredLanePlanVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LanePlanVarianceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LanePlanVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLanePlanVarianceAdjuster(var step: Double) : LanePlanVarianceAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LanePlanVariance, value: Double): Double = roundTo(value, step)
}

data class MinimumLanePlanVarianceAdjuster(var minimum: Double) : LanePlanVarianceAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LanePlanVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LocaleLanePlanVarianceAdjuster(var factors: MutableMap<String, Double>) : LanePlanVarianceAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: LanePlanVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lane plan variance steps in order and keeps a trace of each. */
data class LanePlanVarianceEngine(
    var steps: MutableList<LanePlanVarianceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LanePlanVariance): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LanePlanVarianceAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LanePlanVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLanePlanVarianceEngine(): LanePlanVarianceEngine {
    return LanePlanVarianceEngine(
        steps = mutableListOf<LanePlanVarianceAdjuster>(
            TieredLanePlanVarianceAdjuster(
                mutableListOf<Double>(1772.0, 1970.0),
                mutableListOf<Double>(1.17, 1.04),
            ),
            RoundingLanePlanVarianceAdjuster(1.0),
            MinimumLanePlanVarianceAdjuster(46.7),
            LocaleLanePlanVarianceAdjuster(mutableMapOf<String, Double>("retail" to 0.94)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 39105.5,
    )
}
