package com.acme.transport.linehaul.trailer.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the trailer baseline calculation. */
interface TrailerBaselineModifier {
    fun describe(): String
    fun adjust(item: TrailerBaseline, value: Double): Double
}

data class FlaggedTrailerBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerBaselineModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TrailerBaselineRateOffsetAdjuster(var weight: Double) : TrailerBaselineModifier {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: TrailerBaseline, value: Double): Double = value + item.rate * weight
}

data class ChannelTrailerBaselineAdjuster(var factors: MutableMap<String, Double>) : TrailerBaselineModifier {
    override fun describe(): String = "by channel"

    override fun adjust(item: TrailerBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapTrailerBaselineAdjuster(var cap: Double) : TrailerBaselineModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerBaseline, value: Double): Double = minOf(value, cap)
}

data class TieredTrailerBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerBaselineModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingTrailerBaselineAdjuster(var step: Double) : TrailerBaselineModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerBaseline, value: Double): Double = roundTo(value, step)
}

/** Runs the trailer baseline steps in order and keeps a trace of each. */
data class TrailerBaselineEngine(
    var steps: MutableList<TrailerBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TrailerBaseline): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TrailerBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTrailerBaselineEngine(flags: Registry): TrailerBaselineEngine {
    return TrailerBaselineEngine(
        steps = mutableListOf<TrailerBaselineModifier>(
            FlaggedTrailerBaselineAdjuster(flags, "trailer-baseline-legacy", 5.8),
            TrailerBaselineRateOffsetAdjuster(0.01),
            ChannelTrailerBaselineAdjuster(mutableMapOf<String, Double>("retail" to 0.83)),
            CapTrailerBaselineAdjuster(9614.0),
            TieredTrailerBaselineAdjuster(
                mutableListOf<Double>(436.0, 576.0, 3731.0, 8224.0),
                mutableListOf<Double>(1.07, 0.83, 0.98, 0.94),
            ),
            RoundingTrailerBaselineAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 89941.8,
    )
}
