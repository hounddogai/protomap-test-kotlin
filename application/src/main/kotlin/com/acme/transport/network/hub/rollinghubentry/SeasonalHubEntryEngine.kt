package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal hub entry calculation. */
interface SeasonalHubEntryStep {
    fun describe(): String
    fun adjust(item: SeasonalHubEntry, value: Double): Double
}

data class MinimumSeasonalHubEntryAdjuster(var minimum: Double) : SeasonalHubEntryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalHubEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingSeasonalHubEntryAdjuster(var step: Double) : SeasonalHubEntryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalHubEntry, value: Double): Double = roundTo(value, step)
}

data class FlaggedSeasonalHubEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalHubEntryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalHubEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapSeasonalHubEntryAdjuster(var cap: Double) : SeasonalHubEntryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalHubEntry, value: Double): Double = minOf(value, cap)
}

data class PercentSeasonalHubEntryAdjuster(var percent: Double) : SeasonalHubEntryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalHubEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the seasonal hub entry steps in order and keeps a trace of each. */
data class SeasonalHubEntryEngine(
    var steps: MutableList<SeasonalHubEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: SeasonalHubEntry): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalHubEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultSeasonalHubEntryEngine(flags: Registry): SeasonalHubEntryEngine {
    return SeasonalHubEntryEngine(
        steps = mutableListOf<SeasonalHubEntryStep>(
            MinimumSeasonalHubEntryAdjuster(40.7),
            RoundingSeasonalHubEntryAdjuster(5.0),
            FlaggedSeasonalHubEntryAdjuster(flags, "seasonal-hub-entry-scheduled", 5.7),
            CapSeasonalHubEntryAdjuster(10539.0),
            PercentSeasonalHubEntryAdjuster(-17.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 40454.4,
    )
}
