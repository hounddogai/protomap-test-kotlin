package com.acme.transport.lastmile.drop.dropentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal drop entry calculation. */
interface SeasonalDropEntryStep {
    fun describe(): String
    fun adjust(item: SeasonalDropEntry, value: Double): Double
}

data class MinimumSeasonalDropEntryAdjuster(var minimum: Double) : SeasonalDropEntryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalDropEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSeasonalDropEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalDropEntryStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalDropEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSeasonalDropEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalDropEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalDropEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the seasonal drop entry steps in order and keeps a trace of each. */
data class SeasonalDropEntryEngine(
    var steps: MutableList<SeasonalDropEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalDropEntry): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalDropEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalDropEntryEngine(flags: Registry): SeasonalDropEntryEngine {
    return SeasonalDropEntryEngine(
        steps = mutableListOf<SeasonalDropEntryStep>(
            MinimumSeasonalDropEntryAdjuster(0.6),
            FlaggedSeasonalDropEntryAdjuster(flags, "seasonal-drop-entry-global", -9.6),
            TieredSeasonalDropEntryAdjuster(
                mutableListOf<Double>(3469.0, 4041.0, 6621.0),
                mutableListOf<Double>(0.83, 1.1, 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 76131.7,
    )
}
