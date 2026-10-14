package com.acme.transport.safety.coaching.target

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the coaching claim calculation. */
interface CoachingClaimStep {
    fun describe(): String
    fun adjust(item: CoachingClaim, value: Double): Double
}

data class SourceCoachingClaimAdjuster(var factors: MutableMap<String, Double>) : CoachingClaimStep {
    override fun describe(): String = "by source"

    override fun adjust(item: CoachingClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CoachingClaimLoadKgOffsetAdjuster(var weight: Double) : CoachingClaimStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: CoachingClaim, value: Double): Double = value + item.loadKg * weight
}

data class FlaggedCoachingClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CoachingClaimStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CoachingClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the coaching claim steps in order and keeps a trace of each. */
data class CoachingClaimEngine(
    var steps: MutableList<CoachingClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CoachingClaim): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CoachingClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCoachingClaimEngine(flags: Registry): CoachingClaimEngine {
    return CoachingClaimEngine(
        steps = mutableListOf<CoachingClaimStep>(
            SourceCoachingClaimAdjuster(
                mutableMapOf<String, Double>("south" to 1.36, "north" to 0.84, "west" to 1.15),
            ),
            CoachingClaimLoadKgOffsetAdjuster(0.87),
            FlaggedCoachingClaimAdjuster(flags, "coaching-claim-tiered", 0.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 91046.5,
    )
}
