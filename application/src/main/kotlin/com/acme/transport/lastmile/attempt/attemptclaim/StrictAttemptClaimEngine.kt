package com.acme.transport.lastmile.attempt.attemptclaim

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the strict attempt claim calculation. */
interface StrictAttemptClaimStep {
    fun describe(): String
    fun adjust(item: StrictAttemptClaim, value: Double): Double
}

data class MinimumStrictAttemptClaimAdjuster(var minimum: Double) : StrictAttemptClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictAttemptClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class StrictAttemptClaimStartsAtOffsetAdjuster(var weight: Double) : StrictAttemptClaimStep {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: StrictAttemptClaim, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class LaneStrictAttemptClaimAdjuster(var factors: MutableMap<String, Double>) : StrictAttemptClaimStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: StrictAttemptClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the strict attempt claim steps in order and keeps a trace of each. */
data class StrictAttemptClaimEngine(
    var steps: MutableList<StrictAttemptClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: StrictAttemptClaim): Double {
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

    fun evaluateAll(items: MutableList<StrictAttemptClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultStrictAttemptClaimEngine(): StrictAttemptClaimEngine {
    return StrictAttemptClaimEngine(
        steps = mutableListOf<StrictAttemptClaimStep>(
            MinimumStrictAttemptClaimAdjuster(28.1),
            StrictAttemptClaimStartsAtOffsetAdjuster(0.8),
            LaneStrictAttemptClaimAdjuster(mutableMapOf<String, Double>("south" to 1.18, "east" to 1.21)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 38193.6,
    )
}
