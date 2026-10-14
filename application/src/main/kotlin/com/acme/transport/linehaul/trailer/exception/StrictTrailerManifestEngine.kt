package com.acme.transport.linehaul.trailer.exception

import com.acme.transport.kit.flags.Registry

/** One step of the strict trailer manifest calculation. */
interface StrictTrailerManifestAdjuster {
    fun describe(): String
    fun adjust(item: StrictTrailerManifest, value: Double): Double
}

data class StrictTrailerManifestThresholdOffsetAdjuster(var weight: Double) : StrictTrailerManifestAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: StrictTrailerManifest, value: Double): Double = value + item.threshold * weight
}

data class FlaggedStrictTrailerManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictTrailerManifestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictTrailerManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumStrictTrailerManifestAdjuster(var minimum: Double) : StrictTrailerManifestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictTrailerManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredStrictTrailerManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictTrailerManifestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictTrailerManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict trailer manifest steps in order and keeps a trace of each. */
data class StrictTrailerManifestEngine(
    var steps: MutableList<StrictTrailerManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictTrailerManifest): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictTrailerManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<StrictTrailerManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictTrailerManifestEngine(flags: Registry): StrictTrailerManifestEngine {
    return StrictTrailerManifestEngine(
        steps = mutableListOf<StrictTrailerManifestAdjuster>(
            StrictTrailerManifestThresholdOffsetAdjuster(1.18),
            FlaggedStrictTrailerManifestAdjuster(flags, "strict-trailer-manifest-strict", 4.5),
            MinimumStrictTrailerManifestAdjuster(8.9),
            TieredStrictTrailerManifestAdjuster(
                mutableListOf<Double>(4263.0, 5671.0, 8577.0),
                mutableListOf<Double>(0.86, 1.11, 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 41901.1,
    )
}
