package com.acme.transport.linehaul.relay.claim

import com.acme.transport.kit.flags.Registry

/** One step of the seasonal relay request calculation. */
interface SeasonalRelayRequestFactor {
    fun describe(): String
    fun adjust(item: SeasonalRelayRequest, value: Double): Double
}

data class MinimumSeasonalRelayRequestAdjuster(var minimum: Double) : SeasonalRelayRequestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalRelayRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSeasonalRelayRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalRelayRequestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalRelayRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredSeasonalRelayRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalRelayRequestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalRelayRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapSeasonalRelayRequestAdjuster(var cap: Double) : SeasonalRelayRequestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: SeasonalRelayRequest, value: Double): Double = minOf(value, cap)
}

/** Runs the seasonal relay request steps in order and keeps a trace of each. */
data class SeasonalRelayRequestEngine(
    var steps: MutableList<SeasonalRelayRequestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalRelayRequest): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: SeasonalRelayRequestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<SeasonalRelayRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalRelayRequestEngine(flags: Registry): SeasonalRelayRequestEngine {
    return SeasonalRelayRequestEngine(
        steps = mutableListOf<SeasonalRelayRequestFactor>(
            MinimumSeasonalRelayRequestAdjuster(44.4),
            FlaggedSeasonalRelayRequestAdjuster(flags, "seasonal-relay-request-manual", 4.5),
            TieredSeasonalRelayRequestAdjuster(
                mutableListOf<Double>(99.0, 3128.0, 3938.0),
                mutableListOf<Double>(0.86, 1.08, 1.07),
            ),
            CapSeasonalRelayRequestAdjuster(1866.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 41326.4,
    )
}
