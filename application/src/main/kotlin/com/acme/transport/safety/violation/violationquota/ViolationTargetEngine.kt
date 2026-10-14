package com.acme.transport.safety.violation.violationquota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the violation target calculation. */
interface ViolationTargetModifier {
    fun describe(): String
    fun adjust(item: ViolationTarget, value: Double): Double
}

data class ViolationTargetCapacityOffsetAdjuster(var weight: Double) : ViolationTargetModifier {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: ViolationTarget, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class CapViolationTargetAdjuster(var cap: Double) : ViolationTargetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ViolationTarget, value: Double): Double = minOf(value, cap)
}

data class FlaggedViolationTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ViolationTargetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ViolationTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingViolationTargetAdjuster(var step: Double) : ViolationTargetModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationTarget, value: Double): Double = roundTo(value, step)
}

data class MinimumViolationTargetAdjuster(var minimum: Double) : ViolationTargetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ViolationTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the violation target steps in order and keeps a trace of each. */
data class ViolationTargetEngine(
    var steps: MutableList<ViolationTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ViolationTarget): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ViolationTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultViolationTargetEngine(flags: Registry): ViolationTargetEngine {
    return ViolationTargetEngine(
        steps = mutableListOf<ViolationTargetModifier>(
            ViolationTargetCapacityOffsetAdjuster(1.65),
            CapViolationTargetAdjuster(39126.0),
            FlaggedViolationTargetAdjuster(flags, "violation-target-lenient", 2.9),
            RoundingViolationTargetAdjuster(0.05),
            MinimumViolationTargetAdjuster(4.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 48383.6,
    )
}
