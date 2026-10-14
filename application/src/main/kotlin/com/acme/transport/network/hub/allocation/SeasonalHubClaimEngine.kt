package com.acme.transport.network.hub.allocation

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal hub claim calculation. */
interface SeasonalHubClaimFactor {
    fun describe(): String
    fun adjust(item: SeasonalHubClaim, value: Double): Double
}

data class TieredSeasonalHubClaimAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalHubClaimFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalHubClaim, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumSeasonalHubClaimAdjuster(var minimum: Double) : SeasonalHubClaimFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: SeasonalHubClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentSeasonalHubClaimAdjuster(var percent: Double) : SeasonalHubClaimFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SeasonalHubClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingSeasonalHubClaimAdjuster(var step: Double) : SeasonalHubClaimFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalHubClaim, value: Double): Double = roundTo(value, step)
}

/** Runs the seasonal hub claim steps in order and keeps a trace of each. */
data class SeasonalHubClaimEngine(
    var steps: MutableList<SeasonalHubClaimFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: SeasonalHubClaim): Double {
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

    fun evaluateAll(items: MutableList<SeasonalHubClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultSeasonalHubClaimEngine(): SeasonalHubClaimEngine {
    return SeasonalHubClaimEngine(
        steps = mutableListOf<SeasonalHubClaimFactor>(
            TieredSeasonalHubClaimAdjuster(
                mutableListOf<Double>(4459.0, 5828.0, 8529.0),
                mutableListOf<Double>(1.1, 1.15, 1.12),
            ),
            MinimumSeasonalHubClaimAdjuster(18.5),
            PercentSeasonalHubClaimAdjuster(-5.2),
            RoundingSeasonalHubClaimAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 84865.5,
    )
}
