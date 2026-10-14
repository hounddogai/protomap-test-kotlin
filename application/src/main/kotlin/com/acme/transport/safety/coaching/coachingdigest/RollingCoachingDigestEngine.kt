package com.acme.transport.safety.coaching.coachingdigest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling coaching digest calculation. */
interface RollingCoachingDigestFactor {
    fun describe(): String
    fun adjust(item: RollingCoachingDigest, value: Double): Double
}

data class CapRollingCoachingDigestAdjuster(var cap: Double) : RollingCoachingDigestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingCoachingDigest, value: Double): Double = minOf(value, cap)
}

data class LocaleRollingCoachingDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingCoachingDigestFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: RollingCoachingDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RollingCoachingDigestBudgetOffsetAdjuster(var weight: Double) : RollingCoachingDigestFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: RollingCoachingDigest, value: Double): Double = value + item.budget * weight
}

data class MinimumRollingCoachingDigestAdjuster(var minimum: Double) : RollingCoachingDigestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingCoachingDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling coaching digest steps in order and keeps a trace of each. */
data class RollingCoachingDigestEngine(
    var steps: MutableList<RollingCoachingDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingCoachingDigest): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingCoachingDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingCoachingDigestEngine(): RollingCoachingDigestEngine {
    return RollingCoachingDigestEngine(
        steps = mutableListOf<RollingCoachingDigestFactor>(
            CapRollingCoachingDigestAdjuster(17035.0),
            LocaleRollingCoachingDigestAdjuster(
                mutableMapOf<String, Double>("online" to 1.3, "north" to 0.75, "retail" to 1.36),
            ),
            RollingCoachingDigestBudgetOffsetAdjuster(1.5),
            MinimumRollingCoachingDigestAdjuster(0.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 53883.9,
    )
}
