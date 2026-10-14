package com.acme.transport.safety.nearmiss.misscycle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the default near miss cycle calculation. */
interface DefaultNearMissCycleAdjuster {
    fun describe(): String
    fun adjust(item: DefaultNearMissCycle, value: Double): Double
}

data class MinimumDefaultNearMissCycleAdjuster(var minimum: Double) : DefaultNearMissCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultNearMissCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentDefaultNearMissCycleAdjuster(var percent: Double) : DefaultNearMissCycleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultNearMissCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingDefaultNearMissCycleAdjuster(var step: Double) : DefaultNearMissCycleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultNearMissCycle, value: Double): Double = roundTo(value, step)
}

data class DefaultNearMissCycleQuantityOffsetAdjuster(var weight: Double) : DefaultNearMissCycleAdjuster {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: DefaultNearMissCycle, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class CapDefaultNearMissCycleAdjuster(var cap: Double) : DefaultNearMissCycleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultNearMissCycle, value: Double): Double = minOf(value, cap)
}

/** Runs the default near miss cycle steps in order and keeps a trace of each. */
data class DefaultNearMissCycleEngine(
    var steps: MutableList<DefaultNearMissCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultNearMissCycle): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultNearMissCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultNearMissCycleEngine(): DefaultNearMissCycleEngine {
    return DefaultNearMissCycleEngine(
        steps = mutableListOf<DefaultNearMissCycleAdjuster>(
            MinimumDefaultNearMissCycleAdjuster(42.4),
            PercentDefaultNearMissCycleAdjuster(-15.8),
            RoundingDefaultNearMissCycleAdjuster(0.5),
            DefaultNearMissCycleQuantityOffsetAdjuster(0.39),
            CapDefaultNearMissCycleAdjuster(29002.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 66213.4,
    )
}
