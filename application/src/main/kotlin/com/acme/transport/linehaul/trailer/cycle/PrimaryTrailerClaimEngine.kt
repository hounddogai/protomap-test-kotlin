package com.acme.transport.linehaul.trailer.cycle

import com.acme.transport.kit.flags.Registry

/** One step of the primary trailer claim calculation. */
interface PrimaryTrailerClaimModifier {
    fun describe(): String
    fun adjust(item: PrimaryTrailerClaim, value: Double): Double
}

data class FlaggedPrimaryTrailerClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryTrailerClaimModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryTrailerClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPrimaryTrailerClaimAdjuster(var cap: Double) : PrimaryTrailerClaimModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryTrailerClaim, value: Double): Double = minOf(value, cap)
}

data class PrimaryTrailerClaimMaximumOffsetAdjuster(var weight: Double) : PrimaryTrailerClaimModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: PrimaryTrailerClaim, value: Double): Double = value + item.maximum * weight
}

data class TieredPrimaryTrailerClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryTrailerClaimModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryTrailerClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary trailer claim steps in order and keeps a trace of each. */
data class PrimaryTrailerClaimEngine(
    var steps: MutableList<PrimaryTrailerClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryTrailerClaim): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PrimaryTrailerClaimModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PrimaryTrailerClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryTrailerClaimEngine(flags: Registry): PrimaryTrailerClaimEngine {
    return PrimaryTrailerClaimEngine(
        steps = mutableListOf<PrimaryTrailerClaimModifier>(
            FlaggedPrimaryTrailerClaimAdjuster(flags, "primary-trailer-claim-pending", -2.7),
            CapPrimaryTrailerClaimAdjuster(39114.0),
            PrimaryTrailerClaimMaximumOffsetAdjuster(0.33),
            TieredPrimaryTrailerClaimAdjuster(
                mutableListOf<Double>(5900.0, 6723.0, 8339.0),
                mutableListOf<Double>(0.82, 0.89, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 70761.5,
    )
}
