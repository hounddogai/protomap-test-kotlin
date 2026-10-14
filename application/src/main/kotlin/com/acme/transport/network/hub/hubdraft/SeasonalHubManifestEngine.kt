package com.acme.transport.network.hub.hubdraft

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal hub manifest calculation. */
interface SeasonalHubManifestAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalHubManifest, value: Double): Double
}

data class MinimumSeasonalHubManifestAdjuster(var minimum: Double) : SeasonalHubManifestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalHubManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSeasonalHubManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalHubManifestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalHubManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TitleSeasonalHubManifestAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalHubManifestAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: SeasonalHubManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the seasonal hub manifest steps in order and keeps a trace of each. */
data class SeasonalHubManifestEngine(
    var steps: MutableList<SeasonalHubManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalHubManifest): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalHubManifestAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalHubManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalHubManifestEngine(flags: Registry): SeasonalHubManifestEngine {
    return SeasonalHubManifestEngine(
        steps = mutableListOf<SeasonalHubManifestAdjuster>(
            MinimumSeasonalHubManifestAdjuster(8.4),
            FlaggedSeasonalHubManifestAdjuster(flags, "seasonal-hub-manifest-primary", -2.8),
            TitleSeasonalHubManifestAdjuster(mutableMapOf<String, Double>("north" to 0.8)),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 33493.5,
    )
}
