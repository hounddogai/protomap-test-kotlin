package com.acme.transport.lastmile.attempt.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the attempt bundle calculation. */
interface AttemptBundleFactor {
    fun describe(): String
    fun adjust(item: AttemptBundle, value: Double): Double
}

data class FlaggedAttemptBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AttemptBundleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AttemptBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingAttemptBundleAdjuster(var step: Double) : AttemptBundleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AttemptBundle, value: Double): Double = roundTo(value, step)
}

data class TieredAttemptBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AttemptBundleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AttemptBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the attempt bundle steps in order and keeps a trace of each. */
data class AttemptBundleEngine(
    var steps: MutableList<AttemptBundleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AttemptBundle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AttemptBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAttemptBundleEngine(flags: Registry): AttemptBundleEngine {
    return AttemptBundleEngine(
        steps = mutableListOf<AttemptBundleFactor>(
            FlaggedAttemptBundleAdjuster(flags, "attempt-bundle-primary", 4.3),
            RoundingAttemptBundleAdjuster(0.1),
            TieredAttemptBundleAdjuster(
                mutableListOf<Double>(3757.0, 3836.0, 3853.0, 5555.0),
                mutableListOf<Double>(0.88, 1.14, 0.99, 0.82),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 13567.9,
    )
}
