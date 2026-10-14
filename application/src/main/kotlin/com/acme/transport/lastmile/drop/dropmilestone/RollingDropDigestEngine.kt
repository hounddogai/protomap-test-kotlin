package com.acme.transport.lastmile.drop.dropmilestone

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rolling drop digest calculation. */
interface RollingDropDigestAdjuster {
    fun describe(): String
    fun adjust(item: RollingDropDigest, value: Double): Double
}

data class NameRollingDropDigestAdjuster(var factors: MutableMap<String, Double>) : RollingDropDigestAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: RollingDropDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RollingDropDigestCeilingOffsetAdjuster(var weight: Double) : RollingDropDigestAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: RollingDropDigest, value: Double): Double = value + item.ceiling * weight
}

data class MinimumRollingDropDigestAdjuster(var minimum: Double) : RollingDropDigestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingDropDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling drop digest steps in order and keeps a trace of each. */
data class RollingDropDigestEngine(
    var steps: MutableList<RollingDropDigestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingDropDigest): Double {
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

    fun evaluateAll(items: MutableList<RollingDropDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingDropDigestEngine(): RollingDropDigestEngine {
    return RollingDropDigestEngine(
        steps = mutableListOf<RollingDropDigestAdjuster>(
            NameRollingDropDigestAdjuster(mutableMapOf<String, Double>("east" to 1.37, "retail" to 0.85)),
            RollingDropDigestCeilingOffsetAdjuster(1.4),
            MinimumRollingDropDigestAdjuster(37.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 93886.1,
    )
}
