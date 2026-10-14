package com.acme.transport.safety.coaching.metric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the coaching metric calculation. */
interface CoachingMetricFactor {
    fun describe(): String
    fun adjust(item: CoachingMetric, value: Double): Double
}

data class PercentCoachingMetricAdjuster(var percent: Double) : CoachingMetricFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CoachingMetric, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedCoachingMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CoachingMetricFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CoachingMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCoachingMetricAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CoachingMetricFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CoachingMetric, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCoachingMetricAdjuster(var minimum: Double) : CoachingMetricFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CoachingMetric, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CoachingMetricCapacityOffsetAdjuster(var weight: Double) : CoachingMetricFactor {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: CoachingMetric, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class RoundingCoachingMetricAdjuster(var step: Double) : CoachingMetricFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CoachingMetric, value: Double): Double = roundTo(value, step)
}

/** Runs the coaching metric steps in order and keeps a trace of each. */
data class CoachingMetricEngine(
    var steps: MutableList<CoachingMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CoachingMetric): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CoachingMetricFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CoachingMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCoachingMetricEngine(flags: Registry): CoachingMetricEngine {
    return CoachingMetricEngine(
        steps = mutableListOf<CoachingMetricFactor>(
            PercentCoachingMetricAdjuster(18.7),
            FlaggedCoachingMetricAdjuster(flags, "coaching-metric-weighted", -5.0),
            TieredCoachingMetricAdjuster(
                mutableListOf<Double>(1889.0, 4830.0, 6067.0, 6920.0),
                mutableListOf<Double>(0.98, 1.18, 1.13, 1.03),
            ),
            MinimumCoachingMetricAdjuster(46.3),
            CoachingMetricCapacityOffsetAdjuster(1.09),
            RoundingCoachingMetricAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 21112.6,
    )
}
