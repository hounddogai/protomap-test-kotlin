package com.acme.transport.linehaul.laneplan.tier

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive lane plan exception calculation. */
interface AdaptiveLanePlanExceptionFactor {
    fun describe(): String
    fun adjust(item: AdaptiveLanePlanException, value: Double): Double
}

data class TieredAdaptiveLanePlanExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveLanePlanExceptionFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveLanePlanException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AdaptiveLanePlanExceptionRateOffsetAdjuster(var weight: Double) : AdaptiveLanePlanExceptionFactor {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: AdaptiveLanePlanException, value: Double): Double = value + item.rate * weight
}

data class RoundingAdaptiveLanePlanExceptionAdjuster(var step: Double) : AdaptiveLanePlanExceptionFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveLanePlanException, value: Double): Double = roundTo(value, step)
}

data class FlaggedAdaptiveLanePlanExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveLanePlanExceptionFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveLanePlanException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapAdaptiveLanePlanExceptionAdjuster(var cap: Double) : AdaptiveLanePlanExceptionFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveLanePlanException, value: Double): Double = minOf(value, cap)
}

/** Runs the adaptive lane plan exception steps in order and keeps a trace of each. */
data class AdaptiveLanePlanExceptionEngine(
    var steps: MutableList<AdaptiveLanePlanExceptionFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveLanePlanException): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveLanePlanException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveLanePlanExceptionEngine(flags: Registry): AdaptiveLanePlanExceptionEngine {
    return AdaptiveLanePlanExceptionEngine(
        steps = mutableListOf<AdaptiveLanePlanExceptionFactor>(
            TieredAdaptiveLanePlanExceptionAdjuster(
                mutableListOf<Double>(1324.0, 3548.0, 7469.0),
                mutableListOf<Double>(0.9, 1.18, 1.0),
            ),
            AdaptiveLanePlanExceptionRateOffsetAdjuster(1.08),
            RoundingAdaptiveLanePlanExceptionAdjuster(0.25),
            FlaggedAdaptiveLanePlanExceptionAdjuster(flags, "adaptive-lane-plan-exception-default", -5.1),
            CapAdaptiveLanePlanExceptionAdjuster(9821.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 12031.3,
    )
}
