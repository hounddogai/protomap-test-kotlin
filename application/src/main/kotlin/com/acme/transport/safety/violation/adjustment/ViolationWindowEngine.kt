package com.acme.transport.safety.violation.adjustment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the violation window calculation. */
interface ViolationWindowAdjuster {
    fun describe(): String
    fun adjust(item: ViolationWindow, value: Double): Double
}

data class MinimumViolationWindowAdjuster(var minimum: Double) : ViolationWindowAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ViolationWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentViolationWindowAdjuster(var percent: Double) : ViolationWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ViolationWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingViolationWindowAdjuster(var step: Double) : ViolationWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationWindow, value: Double): Double = roundTo(value, step)
}

data class ViolationWindowThresholdOffsetAdjuster(var weight: Double) : ViolationWindowAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: ViolationWindow, value: Double): Double = value + item.threshold * weight
}

data class CapViolationWindowAdjuster(var cap: Double) : ViolationWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ViolationWindow, value: Double): Double = minOf(value, cap)
}

/** Runs the violation window steps in order and keeps a trace of each. */
data class ViolationWindowEngine(
    var steps: MutableList<ViolationWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ViolationWindow): Double {
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

    fun evaluateAll(items: MutableList<ViolationWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultViolationWindowEngine(): ViolationWindowEngine {
    return ViolationWindowEngine(
        steps = mutableListOf<ViolationWindowAdjuster>(
            MinimumViolationWindowAdjuster(46.8),
            PercentViolationWindowAdjuster(-3.3),
            RoundingViolationWindowAdjuster(0.05),
            ViolationWindowThresholdOffsetAdjuster(0.32),
            CapViolationWindowAdjuster(1995.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 36915.3,
    )
}
