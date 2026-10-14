package com.acme.transport.terminal.traileryard.quota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the trailer yard tier calculation. */
interface TrailerYardTierAdjuster {
    fun describe(): String
    fun adjust(item: TrailerYardTier, value: Double): Double
}

data class FlaggedTrailerYardTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerYardTierAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerYardTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentTrailerYardTierAdjuster(var percent: Double) : TrailerYardTierAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerYardTier, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingTrailerYardTierAdjuster(var step: Double) : TrailerYardTierAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerYardTier, value: Double): Double = roundTo(value, step)
}

/** Runs the trailer yard tier steps in order and keeps a trace of each. */
data class TrailerYardTierEngine(
    var steps: MutableList<TrailerYardTierAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TrailerYardTier): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TrailerYardTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTrailerYardTierEngine(flags: Registry): TrailerYardTierEngine {
    return TrailerYardTierEngine(
        steps = mutableListOf<TrailerYardTierAdjuster>(
            FlaggedTrailerYardTierAdjuster(flags, "trailer-yard-tier-default", -5.3),
            PercentTrailerYardTierAdjuster(2.0),
            RoundingTrailerYardTierAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 1063.1,
    )
}
