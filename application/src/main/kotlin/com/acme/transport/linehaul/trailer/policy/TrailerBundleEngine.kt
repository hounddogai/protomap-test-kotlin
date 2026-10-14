package com.acme.transport.linehaul.trailer.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the trailer bundle calculation. */
interface TrailerBundleStep {
    fun describe(): String
    fun adjust(item: TrailerBundle, value: Double): Double
}

data class TrailerBundleAmountOffsetAdjuster(var weight: Double) : TrailerBundleStep {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: TrailerBundle, value: Double): Double = value + item.amount * weight
}

data class NameTrailerBundleAdjuster(var factors: MutableMap<String, Double>) : TrailerBundleStep {
    override fun describe(): String = "by name"

    override fun adjust(item: TrailerBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedTrailerBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerBundleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapTrailerBundleAdjuster(var cap: Double) : TrailerBundleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerBundle, value: Double): Double = minOf(value, cap)
}

data class MinimumTrailerBundleAdjuster(var minimum: Double) : TrailerBundleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredTrailerBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerBundleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the trailer bundle steps in order and keeps a trace of each. */
data class TrailerBundleEngine(
    var steps: MutableList<TrailerBundleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TrailerBundle): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TrailerBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTrailerBundleEngine(flags: Registry): TrailerBundleEngine {
    return TrailerBundleEngine(
        steps = mutableListOf<TrailerBundleStep>(
            TrailerBundleAmountOffsetAdjuster(1.88),
            NameTrailerBundleAdjuster(mutableMapOf<String, Double>("central" to 1.23)),
            FlaggedTrailerBundleAdjuster(flags, "trailer-bundle-pending", -6.2),
            CapTrailerBundleAdjuster(24137.0),
            MinimumTrailerBundleAdjuster(19.6),
            TieredTrailerBundleAdjuster(
                mutableListOf<Double>(3946.0, 5996.0),
                mutableListOf<Double>(0.84, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 88519.1,
    )
}
