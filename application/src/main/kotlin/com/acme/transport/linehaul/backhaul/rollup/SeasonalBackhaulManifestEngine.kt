package com.acme.transport.linehaul.backhaul.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the seasonal backhaul manifest calculation. */
interface SeasonalBackhaulManifestFactor {
    fun describe(): String
    fun adjust(item: SeasonalBackhaulManifest, value: Double): Double
}

data class MinimumSeasonalBackhaulManifestAdjuster(var minimum: Double) : SeasonalBackhaulManifestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalBackhaulManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SourceSeasonalBackhaulManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalBackhaulManifestFactor {
    override fun describe(): String = "by source"

    override fun adjust(item: SeasonalBackhaulManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredSeasonalBackhaulManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalBackhaulManifestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalBackhaulManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedSeasonalBackhaulManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalBackhaulManifestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalBackhaulManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingSeasonalBackhaulManifestAdjuster(var step: Double) : SeasonalBackhaulManifestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalBackhaulManifest, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal backhaul manifest steps in order and keeps a trace of each. */
data class SeasonalBackhaulManifestEngine(
    var steps: MutableList<SeasonalBackhaulManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalBackhaulManifest): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalBackhaulManifestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalBackhaulManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalBackhaulManifestEngine(flags: Registry): SeasonalBackhaulManifestEngine {
    return SeasonalBackhaulManifestEngine(
        steps = mutableListOf<SeasonalBackhaulManifestFactor>(
            MinimumSeasonalBackhaulManifestAdjuster(32.9),
            SourceSeasonalBackhaulManifestAdjuster(mutableMapOf<String, Double>("south" to 1.31)),
            TieredSeasonalBackhaulManifestAdjuster(
                mutableListOf<Double>(538.0, 3301.0, 4144.0, 5534.0),
                mutableListOf<Double>(1.12, 1.13, 0.8, 1.2),
            ),
            FlaggedSeasonalBackhaulManifestAdjuster(flags, "seasonal-backhaul-manifest-scheduled", 1.1),
            RoundingSeasonalBackhaulManifestAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 61553.6,
    )
}
