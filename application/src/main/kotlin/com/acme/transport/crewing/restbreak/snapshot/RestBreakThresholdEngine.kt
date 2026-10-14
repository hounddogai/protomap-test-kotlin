package com.acme.transport.crewing.restbreak.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rest break threshold calculation. */
interface RestBreakThresholdFactor {
    fun describe(): String
    fun adjust(item: RestBreakThreshold, value: Double): Double
}

data class TieredRestBreakThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RestBreakThresholdFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RestBreakThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RestBreakThresholdThresholdOffsetAdjuster(var weight: Double) : RestBreakThresholdFactor {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: RestBreakThreshold, value: Double): Double = value + item.threshold * weight
}

data class PercentRestBreakThresholdAdjuster(var percent: Double) : RestBreakThresholdFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RestBreakThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRestBreakThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakThresholdFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRestBreakThresholdAdjuster(var minimum: Double) : RestBreakThresholdFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RestBreakThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rest break threshold steps in order and keeps a trace of each. */
data class RestBreakThresholdEngine(
    var steps: MutableList<RestBreakThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RestBreakThreshold): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RestBreakThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRestBreakThresholdEngine(flags: Registry): RestBreakThresholdEngine {
    return RestBreakThresholdEngine(
        steps = mutableListOf<RestBreakThresholdFactor>(
            TieredRestBreakThresholdAdjuster(
                mutableListOf<Double>(2021.0, 3549.0, 6564.0),
                mutableListOf<Double>(1.01, 1.03, 0.88),
            ),
            RestBreakThresholdThresholdOffsetAdjuster(0.19),
            PercentRestBreakThresholdAdjuster(19.8),
            FlaggedRestBreakThresholdAdjuster(flags, "rest-break-threshold-default", -4.5),
            MinimumRestBreakThresholdAdjuster(6.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 82694.2,
    )
}
