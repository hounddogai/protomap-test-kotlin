package com.acme.transport.lastmile.attempt.digest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the attempt digest calculation. */
interface AttemptDigestModifier {
    fun describe(): String
    fun adjust(item: AttemptDigest, value: Double): Double
}

data class RoundingAttemptDigestAdjuster(var step: Double) : AttemptDigestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AttemptDigest, value: Double): Double = roundTo(value, step)
}

data class TieredAttemptDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AttemptDigestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AttemptDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapAttemptDigestAdjuster(var cap: Double) : AttemptDigestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AttemptDigest, value: Double): Double = minOf(value, cap)
}

data class FlaggedAttemptDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AttemptDigestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AttemptDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the attempt digest steps in order and keeps a trace of each. */
data class AttemptDigestEngine(
    var steps: MutableList<AttemptDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AttemptDigest): Double {
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

    fun evaluateAll(items: MutableList<AttemptDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAttemptDigestEngine(flags: Registry): AttemptDigestEngine {
    return AttemptDigestEngine(
        steps = mutableListOf<AttemptDigestModifier>(
            RoundingAttemptDigestAdjuster(0.1),
            TieredAttemptDigestAdjuster(
                mutableListOf<Double>(2644.0, 8876.0),
                mutableListOf<Double>(0.85, 0.86),
            ),
            CapAttemptDigestAdjuster(14329.0),
            FlaggedAttemptDigestAdjuster(flags, "attempt-digest-manual", 3.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 39034.5,
    )
}
