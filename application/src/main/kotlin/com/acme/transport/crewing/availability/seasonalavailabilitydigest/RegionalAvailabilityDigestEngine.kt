package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the regional availability digest calculation. */
interface RegionalAvailabilityDigestModifier {
    fun describe(): String
    fun adjust(item: RegionalAvailabilityDigest, value: Double): Double
}

data class RoundingRegionalAvailabilityDigestAdjuster(var step: Double) : RegionalAvailabilityDigestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalAvailabilityDigest, value: Double): Double = roundTo(value, step)
}

data class TieredRegionalAvailabilityDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalAvailabilityDigestModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalAvailabilityDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRegionalAvailabilityDigestAdjuster(var minimum: Double) : RegionalAvailabilityDigestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalAvailabilityDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRegionalAvailabilityDigestAdjuster(var percent: Double) : RegionalAvailabilityDigestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalAvailabilityDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRegionalAvailabilityDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalAvailabilityDigestModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalAvailabilityDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RegionalAvailabilityDigestAmountOffsetAdjuster(
    var weight: Double,
) : RegionalAvailabilityDigestModifier {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: RegionalAvailabilityDigest, value: Double): Double = value + item.amount * weight
}

/** Runs the regional availability digest steps in order and keeps a trace of each. */
data class RegionalAvailabilityDigestEngine(
    var steps: MutableList<RegionalAvailabilityDigestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalAvailabilityDigest): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RegionalAvailabilityDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalAvailabilityDigestEngine(flags: Registry): RegionalAvailabilityDigestEngine {
    return RegionalAvailabilityDigestEngine(
        steps = mutableListOf<RegionalAvailabilityDigestModifier>(
            RoundingRegionalAvailabilityDigestAdjuster(5.0),
            TieredRegionalAvailabilityDigestAdjuster(
                mutableListOf<Double>(3134.0, 3687.0, 5194.0, 5219.0),
                mutableListOf<Double>(1.1, 1.14, 1.04, 0.94),
            ),
            MinimumRegionalAvailabilityDigestAdjuster(8.2),
            PercentRegionalAvailabilityDigestAdjuster(14.0),
            FlaggedRegionalAvailabilityDigestAdjuster(flags, "regional-availability-digest-fallback", -0.4),
            RegionalAvailabilityDigestAmountOffsetAdjuster(1.87),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 57876.4,
    )
}
