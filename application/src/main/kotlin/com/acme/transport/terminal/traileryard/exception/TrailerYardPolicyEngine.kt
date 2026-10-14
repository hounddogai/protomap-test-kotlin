package com.acme.transport.terminal.traileryard.exception

import com.acme.transport.kit.numbers.roundTo

/** One step of the trailer yard policy calculation. */
interface TrailerYardPolicyStep {
    fun describe(): String
    fun adjust(item: TrailerYardPolicy, value: Double): Double
}

data class PercentTrailerYardPolicyAdjuster(var percent: Double) : TrailerYardPolicyStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerYardPolicy, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredTrailerYardPolicyAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerYardPolicyStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerYardPolicy, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapTrailerYardPolicyAdjuster(var cap: Double) : TrailerYardPolicyStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerYardPolicy, value: Double): Double = minOf(value, cap)
}

data class RoundingTrailerYardPolicyAdjuster(var step: Double) : TrailerYardPolicyStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TrailerYardPolicy, value: Double): Double = roundTo(value, step)
}

/** Runs the trailer yard policy steps in order and keeps a trace of each. */
data class TrailerYardPolicyEngine(
    var steps: MutableList<TrailerYardPolicyStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TrailerYardPolicy): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TrailerYardPolicyStep = steps[index]
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

    fun evaluateAll(items: MutableList<TrailerYardPolicy>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTrailerYardPolicyEngine(): TrailerYardPolicyEngine {
    return TrailerYardPolicyEngine(
        steps = mutableListOf<TrailerYardPolicyStep>(
            PercentTrailerYardPolicyAdjuster(8.1),
            TieredTrailerYardPolicyAdjuster(
                mutableListOf<Double>(5946.0, 6803.0, 7139.0, 7396.0),
                mutableListOf<Double>(1.08, 0.93, 1.15, 1.14),
            ),
            CapTrailerYardPolicyAdjuster(13256.0),
            RoundingTrailerYardPolicyAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 6056.6,
    )
}
