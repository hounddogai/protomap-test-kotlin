package com.acme.transport.linehaul.laneplan.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient lane plan estimate calculation. */
interface LenientLanePlanEstimateAdjuster {
    fun describe(): String
    fun adjust(item: LenientLanePlanEstimate, value: Double): Double
}

data class FlaggedLenientLanePlanEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientLanePlanEstimateAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientLanePlanEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLenientLanePlanEstimateAdjuster(var minimum: Double) : LenientLanePlanEstimateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientLanePlanEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TitleLenientLanePlanEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientLanePlanEstimateAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: LenientLanePlanEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLenientLanePlanEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LenientLanePlanEstimateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LenientLanePlanEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLenientLanePlanEstimateAdjuster(var step: Double) : LenientLanePlanEstimateAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientLanePlanEstimate, value: Double): Double = roundTo(value, step)
}

data class LenientLanePlanEstimateRateOffsetAdjuster(var weight: Double) : LenientLanePlanEstimateAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: LenientLanePlanEstimate, value: Double): Double = value + item.rate * weight
}

/** Runs the lenient lane plan estimate steps in order and keeps a trace of each. */
data class LenientLanePlanEstimateEngine(
    var steps: MutableList<LenientLanePlanEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LenientLanePlanEstimate): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientLanePlanEstimateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LenientLanePlanEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLenientLanePlanEstimateEngine(flags: Registry): LenientLanePlanEstimateEngine {
    return LenientLanePlanEstimateEngine(
        steps = mutableListOf<LenientLanePlanEstimateAdjuster>(
            FlaggedLenientLanePlanEstimateAdjuster(flags, "lenient-lane-plan-estimate-regional", 2.6),
            MinimumLenientLanePlanEstimateAdjuster(8.9),
            TitleLenientLanePlanEstimateAdjuster(mutableMapOf<String, Double>("west" to 0.7)),
            TieredLenientLanePlanEstimateAdjuster(
                mutableListOf<Double>(5568.0, 6546.0),
                mutableListOf<Double>(1.03, 0.83),
            ),
            RoundingLenientLanePlanEstimateAdjuster(0.25),
            LenientLanePlanEstimateRateOffsetAdjuster(0.39),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 2240.4,
    )
}
