package com.acme.transport.terminal.traileryard.tier

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the trailer yard claim calculation. */
interface TrailerYardClaimStep {
    fun describe(): String
    fun adjust(item: TrailerYardClaim, value: Double): Double
}

data class LaneTrailerYardClaimAdjuster(var factors: MutableMap<String, Double>) : TrailerYardClaimStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: TrailerYardClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapTrailerYardClaimAdjuster(var cap: Double) : TrailerYardClaimStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerYardClaim, value: Double): Double = minOf(value, cap)
}

data class MinimumTrailerYardClaimAdjuster(var minimum: Double) : TrailerYardClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TrailerYardClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the trailer yard claim steps in order and keeps a trace of each. */
data class TrailerYardClaimEngine(
    var steps: MutableList<TrailerYardClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TrailerYardClaim): Double {
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

    fun evaluateAll(items: MutableList<TrailerYardClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTrailerYardClaimEngine(): TrailerYardClaimEngine {
    return TrailerYardClaimEngine(
        steps = mutableListOf<TrailerYardClaimStep>(
            LaneTrailerYardClaimAdjuster(mutableMapOf<String, Double>("north" to 0.72, "east" to 1.0)),
            CapTrailerYardClaimAdjuster(27368.0),
            MinimumTrailerYardClaimAdjuster(29.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 32404.6,
    )
}
