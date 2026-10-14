package com.acme.transport.freightpay.ratecard.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rate card estimate calculation. */
interface RateCardEstimateStep {
    fun describe(): String
    fun adjust(item: RateCardEstimate, value: Double): Double
}

data class SegmentRateCardEstimateAdjuster(var factors: MutableMap<String, Double>) : RateCardEstimateStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: RateCardEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingRateCardEstimateAdjuster(var step: Double) : RateCardEstimateStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RateCardEstimate, value: Double): Double = roundTo(value, step)
}

data class FlaggedRateCardEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RateCardEstimateStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RateCardEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRateCardEstimateAdjuster(var minimum: Double) : RateCardEstimateStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RateCardEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapRateCardEstimateAdjuster(var cap: Double) : RateCardEstimateStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RateCardEstimate, value: Double): Double = minOf(value, cap)
}

/** Runs the rate card estimate steps in order and keeps a trace of each. */
data class RateCardEstimateEngine(
    var steps: MutableList<RateCardEstimateStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RateCardEstimate): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RateCardEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRateCardEstimateEngine(flags: Registry): RateCardEstimateEngine {
    return RateCardEstimateEngine(
        steps = mutableListOf<RateCardEstimateStep>(
            SegmentRateCardEstimateAdjuster(mutableMapOf<String, Double>("north" to 1.32)),
            RoundingRateCardEstimateAdjuster(0.05),
            FlaggedRateCardEstimateAdjuster(flags, "rate-card-estimate-fallback", -3.8),
            MinimumRateCardEstimateAdjuster(22.7),
            CapRateCardEstimateAdjuster(19496.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 29393.0,
    )
}
