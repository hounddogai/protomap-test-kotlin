package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the violation entry calculation. */
interface ViolationEntryAdjuster {
    fun describe(): String
    fun adjust(item: ViolationEntry, value: Double): Double
}

data class MinimumViolationEntryAdjuster(var minimum: Double) : ViolationEntryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ViolationEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentViolationEntryAdjuster(var percent: Double) : ViolationEntryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ViolationEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedViolationEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ViolationEntryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ViolationEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingViolationEntryAdjuster(var step: Double) : ViolationEntryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ViolationEntry, value: Double): Double = roundTo(value, step)
}

data class ViolationEntryQuantityOffsetAdjuster(var weight: Double) : ViolationEntryAdjuster {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: ViolationEntry, value: Double): Double = value + item.quantity.toDouble() * weight
}

/** Runs the violation entry steps in order and keeps a trace of each. */
data class ViolationEntryEngine(
    var steps: MutableList<ViolationEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ViolationEntry): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ViolationEntryAdjuster = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ViolationEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultViolationEntryEngine(flags: Registry): ViolationEntryEngine {
    return ViolationEntryEngine(
        steps = mutableListOf<ViolationEntryAdjuster>(
            MinimumViolationEntryAdjuster(20.1),
            PercentViolationEntryAdjuster(-19.3),
            FlaggedViolationEntryAdjuster(flags, "violation-entry-regional", -7.1),
            RoundingViolationEntryAdjuster(5.0),
            ViolationEntryQuantityOffsetAdjuster(1.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 64554.2,
    )
}
