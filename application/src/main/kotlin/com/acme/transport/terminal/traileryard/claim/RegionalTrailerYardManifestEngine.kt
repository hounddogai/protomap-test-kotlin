package com.acme.transport.terminal.traileryard.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the regional trailer yard manifest calculation. */
interface RegionalTrailerYardManifestAdjuster {
    fun describe(): String
    fun adjust(item: RegionalTrailerYardManifest, value: Double): Double
}

data class MinimumRegionalTrailerYardManifestAdjuster(
    var minimum: Double,
) : RegionalTrailerYardManifestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalTrailerYardManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRegionalTrailerYardManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalTrailerYardManifestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalTrailerYardManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RegionalTrailerYardManifestLoadKgOffsetAdjuster(
    var weight: Double,
) : RegionalTrailerYardManifestAdjuster {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: RegionalTrailerYardManifest, value: Double): Double = value + item.loadKg * weight
}

data class FlaggedRegionalTrailerYardManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalTrailerYardManifestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalTrailerYardManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingRegionalTrailerYardManifestAdjuster(var step: Double) : RegionalTrailerYardManifestAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RegionalTrailerYardManifest, value: Double): Double = roundTo(value, step)
}

data class PercentRegionalTrailerYardManifestAdjuster(
    var percent: Double,
) : RegionalTrailerYardManifestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalTrailerYardManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional trailer yard manifest steps in order and keeps a trace of each. */
data class RegionalTrailerYardManifestEngine(
    var steps: MutableList<RegionalTrailerYardManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalTrailerYardManifest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalTrailerYardManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalTrailerYardManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalTrailerYardManifestEngine(flags: Registry): RegionalTrailerYardManifestEngine {
    return RegionalTrailerYardManifestEngine(
        steps = mutableListOf<RegionalTrailerYardManifestAdjuster>(
            MinimumRegionalTrailerYardManifestAdjuster(13.8),
            TieredRegionalTrailerYardManifestAdjuster(
                mutableListOf<Double>(1550.0, 6282.0, 7836.0),
                mutableListOf<Double>(1.17, 0.98, 1.02),
            ),
            RegionalTrailerYardManifestLoadKgOffsetAdjuster(0.59),
            FlaggedRegionalTrailerYardManifestAdjuster(flags, "regional-trailer-yard-manifest-strict", -9.9),
            RoundingRegionalTrailerYardManifestAdjuster(0.5),
            PercentRegionalTrailerYardManifestAdjuster(15.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 64985.7,
    )
}
