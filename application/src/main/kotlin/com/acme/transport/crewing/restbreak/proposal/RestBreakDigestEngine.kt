package com.acme.transport.crewing.restbreak.proposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rest break digest calculation. */
interface RestBreakDigestStep {
    fun describe(): String
    fun adjust(item: RestBreakDigest, value: Double): Double
}

data class PercentRestBreakDigestAdjuster(var percent: Double) : RestBreakDigestStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RestBreakDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRestBreakDigestAdjuster(var cap: Double) : RestBreakDigestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RestBreakDigest, value: Double): Double = minOf(value, cap)
}

data class TieredRestBreakDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RestBreakDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RestBreakDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRestBreakDigestAdjuster(var step: Double) : RestBreakDigestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RestBreakDigest, value: Double): Double = roundTo(value, step)
}

data class FlaggedRestBreakDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumRestBreakDigestAdjuster(var minimum: Double) : RestBreakDigestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RestBreakDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rest break digest steps in order and keeps a trace of each. */
data class RestBreakDigestEngine(
    var steps: MutableList<RestBreakDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RestBreakDigest): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RestBreakDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRestBreakDigestEngine(flags: Registry): RestBreakDigestEngine {
    return RestBreakDigestEngine(
        steps = mutableListOf<RestBreakDigestStep>(
            PercentRestBreakDigestAdjuster(-21.8),
            CapRestBreakDigestAdjuster(38957.0),
            TieredRestBreakDigestAdjuster(
                mutableListOf<Double>(2496.0, 3255.0),
                mutableListOf<Double>(0.83, 1.0),
            ),
            RoundingRestBreakDigestAdjuster(0.1),
            FlaggedRestBreakDigestAdjuster(flags, "rest-break-digest-global", 1.2),
            MinimumRestBreakDigestAdjuster(23.6),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 83514.5,
    )
}
