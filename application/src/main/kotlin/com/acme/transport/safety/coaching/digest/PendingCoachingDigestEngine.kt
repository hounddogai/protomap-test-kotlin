package com.acme.transport.safety.coaching.digest

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending coaching digest calculation. */
interface PendingCoachingDigestStep {
    fun describe(): String
    fun adjust(item: PendingCoachingDigest, value: Double): Double
}

data class RegionPendingCoachingDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingCoachingDigestStep {
    override fun describe(): String = "by region"

    override fun adjust(item: PendingCoachingDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumPendingCoachingDigestAdjuster(var minimum: Double) : PendingCoachingDigestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingCoachingDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PendingCoachingDigestMaximumOffsetAdjuster(var weight: Double) : PendingCoachingDigestStep {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: PendingCoachingDigest, value: Double): Double = value + item.maximum * weight
}

data class TieredPendingCoachingDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingCoachingDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingCoachingDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the pending coaching digest steps in order and keeps a trace of each. */
data class PendingCoachingDigestEngine(
    var steps: MutableList<PendingCoachingDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingCoachingDigest): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingCoachingDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingCoachingDigestEngine(): PendingCoachingDigestEngine {
    return PendingCoachingDigestEngine(
        steps = mutableListOf<PendingCoachingDigestStep>(
            RegionPendingCoachingDigestAdjuster(
                mutableMapOf<String, Double>("north" to 0.79, "central" to 0.81),
            ),
            MinimumPendingCoachingDigestAdjuster(20.7),
            PendingCoachingDigestMaximumOffsetAdjuster(0.91),
            TieredPendingCoachingDigestAdjuster(
                mutableListOf<Double>(2662.0, 8053.0),
                mutableListOf<Double>(1.12, 1.08),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 44736.4,
    )
}
