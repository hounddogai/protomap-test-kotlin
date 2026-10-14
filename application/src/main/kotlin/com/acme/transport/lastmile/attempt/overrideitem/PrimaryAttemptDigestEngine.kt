package com.acme.transport.lastmile.attempt.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary attempt digest calculation. */
interface PrimaryAttemptDigestModifier {
    fun describe(): String
    fun adjust(item: PrimaryAttemptDigest, value: Double): Double
}

data class TieredPrimaryAttemptDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryAttemptDigestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryAttemptDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumPrimaryAttemptDigestAdjuster(var minimum: Double) : PrimaryAttemptDigestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryAttemptDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PrimaryAttemptDigestThresholdOffsetAdjuster(var weight: Double) : PrimaryAttemptDigestModifier {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: PrimaryAttemptDigest, value: Double): Double = value + item.threshold * weight
}

data class RoundingPrimaryAttemptDigestAdjuster(var step: Double) : PrimaryAttemptDigestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryAttemptDigest, value: Double): Double = roundTo(value, step)
}

data class FlaggedPrimaryAttemptDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryAttemptDigestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryAttemptDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the primary attempt digest steps in order and keeps a trace of each. */
data class PrimaryAttemptDigestEngine(
    var steps: MutableList<PrimaryAttemptDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryAttemptDigest): Double {
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

    fun evaluateAll(items: MutableList<PrimaryAttemptDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryAttemptDigestEngine(flags: Registry): PrimaryAttemptDigestEngine {
    return PrimaryAttemptDigestEngine(
        steps = mutableListOf<PrimaryAttemptDigestModifier>(
            TieredPrimaryAttemptDigestAdjuster(
                mutableListOf<Double>(398.0, 2089.0),
                mutableListOf<Double>(1.01, 0.82),
            ),
            MinimumPrimaryAttemptDigestAdjuster(37.5),
            PrimaryAttemptDigestThresholdOffsetAdjuster(1.84),
            RoundingPrimaryAttemptDigestAdjuster(1.0),
            FlaggedPrimaryAttemptDigestAdjuster(flags, "primary-attempt-digest-strict", -9.7),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 81773.5,
    )
}
