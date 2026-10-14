package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the seasonal trailer yard policy calculation. */
interface SeasonalTrailerYardPolicyAdjuster {
    fun describe(): String
    fun adjust(item: SeasonalTrailerYardPolicy, value: Double): Double
}

data class TieredSeasonalTrailerYardPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SeasonalTrailerYardPolicyAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SeasonalTrailerYardPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingSeasonalTrailerYardPolicyAdjuster(var step: Double) : SeasonalTrailerYardPolicyAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SeasonalTrailerYardPolicy, value: Double): Double = roundTo(value, step)
}

data class FlaggedSeasonalTrailerYardPolicyAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SeasonalTrailerYardPolicyAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SeasonalTrailerYardPolicy, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the seasonal trailer yard policy steps in order and keeps a trace of each. */
data class SeasonalTrailerYardPolicyEngine(
    var steps: MutableList<SeasonalTrailerYardPolicyAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SeasonalTrailerYardPolicy): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<SeasonalTrailerYardPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSeasonalTrailerYardPolicyEngine(flags: Registry): SeasonalTrailerYardPolicyEngine {
    return SeasonalTrailerYardPolicyEngine(
        steps = mutableListOf<SeasonalTrailerYardPolicyAdjuster>(
            TieredSeasonalTrailerYardPolicyAdjuster(
                mutableListOf<Double>(3065.0, 8385.0),
                mutableListOf<Double>(0.96, 1.08),
            ),
            RoundingSeasonalTrailerYardPolicyAdjuster(0.05),
            FlaggedSeasonalTrailerYardPolicyAdjuster(flags, "seasonal-trailer-yard-policy-tiered", -4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 3087.5,
    )
}
