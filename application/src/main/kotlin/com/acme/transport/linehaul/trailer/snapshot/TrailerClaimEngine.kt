package com.acme.transport.linehaul.trailer.snapshot

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the trailer claim calculation. */
interface TrailerClaimModifier {
    fun describe(): String
    fun adjust(item: TrailerClaim, value: Double): Double
}

data class PercentTrailerClaimAdjuster(var percent: Double) : TrailerClaimModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumTrailerClaimAdjuster(var minimum: Double) : TrailerClaimModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TrailerClaimAttemptsOffsetAdjuster(var weight: Double) : TrailerClaimModifier {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: TrailerClaim, value: Double): Double = value + item.attempts.toDouble() * weight
}

/** Runs the trailer claim steps in order and keeps a trace of each. */
data class TrailerClaimEngine(
    var steps: MutableList<TrailerClaimModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TrailerClaim): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TrailerClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTrailerClaimEngine(): TrailerClaimEngine {
    return TrailerClaimEngine(
        steps = mutableListOf<TrailerClaimModifier>(
            PercentTrailerClaimAdjuster(21.1),
            MinimumTrailerClaimAdjuster(3.8),
            TrailerClaimAttemptsOffsetAdjuster(1.61),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 11120.1,
    )
}
