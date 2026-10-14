package com.acme.transport.lastmile.attempt.claim

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the attempt claim calculation. */
interface AttemptClaimAdjuster {
    fun describe(): String
    fun adjust(item: AttemptClaim, value: Double): Double
}

data class MinimumAttemptClaimAdjuster(var minimum: Double) : AttemptClaimAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AttemptClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentAttemptClaimAdjuster(var percent: Double) : AttemptClaimAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AttemptClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AttemptClaimBudgetOffsetAdjuster(var weight: Double) : AttemptClaimAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: AttemptClaim, value: Double): Double = value + item.budget * weight
}

/** Runs the attempt claim steps in order and keeps a trace of each. */
data class AttemptClaimEngine(
    var steps: MutableList<AttemptClaimAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AttemptClaim): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AttemptClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAttemptClaimEngine(): AttemptClaimEngine {
    return AttemptClaimEngine(
        steps = mutableListOf<AttemptClaimAdjuster>(
            MinimumAttemptClaimAdjuster(40.5),
            PercentAttemptClaimAdjuster(17.9),
            AttemptClaimBudgetOffsetAdjuster(1.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 14914.8,
    )
}
