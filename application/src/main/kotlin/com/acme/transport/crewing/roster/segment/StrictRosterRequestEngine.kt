package com.acme.transport.crewing.roster.segment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict roster request calculation. */
interface StrictRosterRequestModifier {
    fun describe(): String
    fun adjust(item: StrictRosterRequest, value: Double): Double
}

data class CapStrictRosterRequestAdjuster(var cap: Double) : StrictRosterRequestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictRosterRequest, value: Double): Double = minOf(value, cap)
}

data class StrictRosterRequestExpiresAtOffsetAdjuster(var weight: Double) : StrictRosterRequestModifier {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: StrictRosterRequest, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class PercentStrictRosterRequestAdjuster(var percent: Double) : StrictRosterRequestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictRosterRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingStrictRosterRequestAdjuster(var step: Double) : StrictRosterRequestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictRosterRequest, value: Double): Double = roundTo(value, step)
}

data class MinimumStrictRosterRequestAdjuster(var minimum: Double) : StrictRosterRequestModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictRosterRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the strict roster request steps in order and keeps a trace of each. */
data class StrictRosterRequestEngine(
    var steps: MutableList<StrictRosterRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictRosterRequest): Double {
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

    fun evaluateAll(items: MutableList<StrictRosterRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictRosterRequestEngine(): StrictRosterRequestEngine {
    return StrictRosterRequestEngine(
        steps = mutableListOf<StrictRosterRequestModifier>(
            CapStrictRosterRequestAdjuster(538.0),
            StrictRosterRequestExpiresAtOffsetAdjuster(1.76),
            PercentStrictRosterRequestAdjuster(3.7),
            RoundingStrictRosterRequestAdjuster(0.5),
            MinimumStrictRosterRequestAdjuster(27.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 73285.1,
    )
}
