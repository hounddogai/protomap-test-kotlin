package com.acme.transport.emissions.routeoffset.offsetmetric

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary route offset digest calculation. */
interface PrimaryRouteOffsetDigestStep {
    fun describe(): String
    fun adjust(item: PrimaryRouteOffsetDigest, value: Double): Double
}

data class MinimumPrimaryRouteOffsetDigestAdjuster(var minimum: Double) : PrimaryRouteOffsetDigestStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryRouteOffsetDigest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PrimaryRouteOffsetDigestAttemptsOffsetAdjuster(var weight: Double) : PrimaryRouteOffsetDigestStep {
    override fun describe(): String = "plus attempts times $weight"

    override fun adjust(item: PrimaryRouteOffsetDigest, value: Double): Double {
        return value + item.attempts.toDouble() * weight
    }
}

data class RoundingPrimaryRouteOffsetDigestAdjuster(var step: Double) : PrimaryRouteOffsetDigestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryRouteOffsetDigest, value: Double): Double = roundTo(value, step)
}

data class NamePrimaryRouteOffsetDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryRouteOffsetDigestStep {
    override fun describe(): String = "by name"

    override fun adjust(item: PrimaryRouteOffsetDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapPrimaryRouteOffsetDigestAdjuster(var cap: Double) : PrimaryRouteOffsetDigestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryRouteOffsetDigest, value: Double): Double = minOf(value, cap)
}

/** Runs the primary route offset digest steps in order and keeps a trace of each. */
data class PrimaryRouteOffsetDigestEngine(
    var steps: MutableList<PrimaryRouteOffsetDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryRouteOffsetDigest): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryRouteOffsetDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryRouteOffsetDigestEngine(): PrimaryRouteOffsetDigestEngine {
    return PrimaryRouteOffsetDigestEngine(
        steps = mutableListOf<PrimaryRouteOffsetDigestStep>(
            MinimumPrimaryRouteOffsetDigestAdjuster(47.8),
            PrimaryRouteOffsetDigestAttemptsOffsetAdjuster(0.57),
            RoundingPrimaryRouteOffsetDigestAdjuster(0.5),
            NamePrimaryRouteOffsetDigestAdjuster(mutableMapOf<String, Double>("west" to 0.98)),
            CapPrimaryRouteOffsetDigestAdjuster(19459.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 52424.6,
    )
}
