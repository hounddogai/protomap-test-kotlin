package com.acme.transport.safety.violation.adjustment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the violation adjustment calculation. */
interface ViolationAdjustmentModifier {
    fun describe(): String
    fun adjust(item: ViolationAdjustment, value: Double): Double
}

data class RoundingViolationAdjustmentAdjuster(var step: Double) : ViolationAdjustmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationAdjustment, value: Double): Double = roundTo(value, step)
}

data class PercentViolationAdjustmentAdjuster(var percent: Double) : ViolationAdjustmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ViolationAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumViolationAdjustmentAdjuster(var minimum: Double) : ViolationAdjustmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ViolationAdjustment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapViolationAdjustmentAdjuster(var cap: Double) : ViolationAdjustmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ViolationAdjustment, value: Double): Double = minOf(value, cap)
}

data class FlaggedViolationAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ViolationAdjustmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ViolationAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class NameViolationAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : ViolationAdjustmentModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: ViolationAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the violation adjustment steps in order and keeps a trace of each. */
data class ViolationAdjustmentEngine(
    var steps: MutableList<ViolationAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ViolationAdjustment): Double {
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

    fun evaluateAll(items: MutableList<ViolationAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultViolationAdjustmentEngine(flags: Registry): ViolationAdjustmentEngine {
    return ViolationAdjustmentEngine(
        steps = mutableListOf<ViolationAdjustmentModifier>(
            RoundingViolationAdjustmentAdjuster(0.1),
            PercentViolationAdjustmentAdjuster(-0.9),
            MinimumViolationAdjustmentAdjuster(29.8),
            CapViolationAdjustmentAdjuster(11925.0),
            FlaggedViolationAdjustmentAdjuster(flags, "violation-adjustment-tiered", -3.0),
            NameViolationAdjustmentAdjuster(
                mutableMapOf<String, Double>("west" to 0.93, "south" to 0.72, "east" to 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 36771.6,
    )
}
