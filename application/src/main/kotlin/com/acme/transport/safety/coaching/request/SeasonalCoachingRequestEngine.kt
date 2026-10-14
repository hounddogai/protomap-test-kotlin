package com.acme.transport.safety.coaching.request

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal coaching request calculation. */
interface SeasonalCoachingRequestFactor {
    fun describe(): String
    fun adjust(item: SeasonalCoachingRequest, value: Double): Double
}

data class TitleSeasonalCoachingRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : SeasonalCoachingRequestFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: SeasonalCoachingRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapSeasonalCoachingRequestAdjuster(var cap: Double) : SeasonalCoachingRequestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalCoachingRequest, value: Double): Double = minOf(value, cap)
}

data class SeasonalCoachingRequestCostOffsetAdjuster(var weight: Double) : SeasonalCoachingRequestFactor {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: SeasonalCoachingRequest, value: Double): Double = value + item.cost * weight
}

data class FlaggedSeasonalCoachingRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalCoachingRequestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalCoachingRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSeasonalCoachingRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalCoachingRequestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalCoachingRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the seasonal coaching request steps in order and keeps a trace of each. */
data class SeasonalCoachingRequestEngine(
    var steps: MutableList<SeasonalCoachingRequestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalCoachingRequest): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalCoachingRequestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalCoachingRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalCoachingRequestEngine(flags: Registry): SeasonalCoachingRequestEngine {
    return SeasonalCoachingRequestEngine(
        steps = mutableListOf<SeasonalCoachingRequestFactor>(
            TitleSeasonalCoachingRequestAdjuster(
                mutableMapOf<String, Double>("east" to 0.74, "online" to 0.8, "south" to 0.95),
            ),
            CapSeasonalCoachingRequestAdjuster(32208.0),
            SeasonalCoachingRequestCostOffsetAdjuster(0.17),
            FlaggedSeasonalCoachingRequestAdjuster(flags, "seasonal-coaching-request-legacy", -8.6),
            TieredSeasonalCoachingRequestAdjuster(
                mutableListOf<Double>(1246.0, 6809.0, 8922.0, 9179.0),
                mutableListOf<Double>(0.95, 1.12, 0.82, 1.19),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 28317.2,
    )
}
