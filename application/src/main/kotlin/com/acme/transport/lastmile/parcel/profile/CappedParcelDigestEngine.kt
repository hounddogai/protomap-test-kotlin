package com.acme.transport.lastmile.parcel.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped parcel digest calculation. */
interface CappedParcelDigestModifier {
    fun describe(): String
    fun adjust(item: CappedParcelDigest, value: Double): Double
}

data class RoundingCappedParcelDigestAdjuster(var step: Double) : CappedParcelDigestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedParcelDigest, value: Double): Double = roundTo(value, step)
}

data class MinimumCappedParcelDigestAdjuster(var minimum: Double) : CappedParcelDigestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedParcelDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedCappedParcelDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedParcelDigestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedParcelDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped parcel digest steps in order and keeps a trace of each. */
data class CappedParcelDigestEngine(
    var steps: MutableList<CappedParcelDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedParcelDigest): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedParcelDigestModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedParcelDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedParcelDigestEngine(flags: Registry): CappedParcelDigestEngine {
    return CappedParcelDigestEngine(
        steps = mutableListOf<CappedParcelDigestModifier>(
            RoundingCappedParcelDigestAdjuster(0.1),
            MinimumCappedParcelDigestAdjuster(26.4),
            FlaggedCappedParcelDigestAdjuster(flags, "capped-parcel-digest-legacy", -0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 64705.8,
    )
}
