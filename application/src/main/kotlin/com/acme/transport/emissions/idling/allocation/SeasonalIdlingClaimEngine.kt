package com.acme.transport.emissions.idling.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the seasonal idling claim calculation. */
interface SeasonalIdlingClaimStep {
    fun describe(): String
    fun adjust(item: SeasonalIdlingClaim, value: Double): Double
}

data class PercentSeasonalIdlingClaimAdjuster(var percent: Double) : SeasonalIdlingClaimStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalIdlingClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumSeasonalIdlingClaimAdjuster(var minimum: Double) : SeasonalIdlingClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalIdlingClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedSeasonalIdlingClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalIdlingClaimStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalIdlingClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal idling claim steps in order and keeps a trace of each. */
data class SeasonalIdlingClaimEngine(
    var steps: MutableList<SeasonalIdlingClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalIdlingClaim): Double {
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

    fun evaluateAll(items: MutableList<SeasonalIdlingClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalIdlingClaimEngine(flags: Registry): SeasonalIdlingClaimEngine {
    return SeasonalIdlingClaimEngine(
        steps = mutableListOf<SeasonalIdlingClaimStep>(
            PercentSeasonalIdlingClaimAdjuster(-22.2),
            MinimumSeasonalIdlingClaimAdjuster(26.6),
            FlaggedSeasonalIdlingClaimAdjuster(flags, "seasonal-idling-claim-seasonal", -6.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 36831.8,
    )
}
