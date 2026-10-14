package com.acme.transport.linehaul.laneplan.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the lane plan estimate calculation. */
interface LanePlanEstimateFactor {
    fun describe(): String
    fun adjust(item: LanePlanEstimate, value: Double): Double
}

data class CapLanePlanEstimateAdjuster(var cap: Double) : LanePlanEstimateFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LanePlanEstimate, value: Double): Double = minOf(value, cap)
}

data class PercentLanePlanEstimateAdjuster(var percent: Double) : LanePlanEstimateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LanePlanEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLanePlanEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LanePlanEstimateFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LanePlanEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelLanePlanEstimateAdjuster(var factors: MutableMap<String, Double>) : LanePlanEstimateFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: LanePlanEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLanePlanEstimateAdjuster(var step: Double) : LanePlanEstimateFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LanePlanEstimate, value: Double): Double = roundTo(value, step)
}

data class TieredLanePlanEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LanePlanEstimateFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LanePlanEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the lane plan estimate steps in order and keeps a trace of each. */
data class LanePlanEstimateEngine(
    var steps: MutableList<LanePlanEstimateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LanePlanEstimate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LanePlanEstimateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LanePlanEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLanePlanEstimateEngine(flags: Registry): LanePlanEstimateEngine {
    return LanePlanEstimateEngine(
        steps = mutableListOf<LanePlanEstimateFactor>(
            CapLanePlanEstimateAdjuster(49418.0),
            PercentLanePlanEstimateAdjuster(21.1),
            FlaggedLanePlanEstimateAdjuster(flags, "lane-plan-estimate-weighted", -3.3),
            LabelLanePlanEstimateAdjuster(mutableMapOf<String, Double>("east" to 1.06)),
            RoundingLanePlanEstimateAdjuster(0.5),
            TieredLanePlanEstimateAdjuster(
                mutableListOf<Double>(5583.0, 6830.0, 8293.0, 9146.0),
                mutableListOf<Double>(0.83, 0.97, 0.81, 0.91),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 13044.3,
    )
}
