package com.acme.transport.linehaul.tender.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the strict tender profile calculation. */
interface StrictTenderProfileModifier {
    fun describe(): String
    fun adjust(item: StrictTenderProfile, value: Double): Double
}

data class RoundingStrictTenderProfileAdjuster(var step: Double) : StrictTenderProfileModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictTenderProfile, value: Double): Double = roundTo(value, step)
}

data class CapStrictTenderProfileAdjuster(var cap: Double) : StrictTenderProfileModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictTenderProfile, value: Double): Double = minOf(value, cap)
}

data class StrictTenderProfileCapacityOffsetAdjuster(var weight: Double) : StrictTenderProfileModifier {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: StrictTenderProfile, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class MinimumStrictTenderProfileAdjuster(var minimum: Double) : StrictTenderProfileModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictTenderProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedStrictTenderProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictTenderProfileModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictTenderProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentStrictTenderProfileAdjuster(var percent: Double) : StrictTenderProfileModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictTenderProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the strict tender profile steps in order and keeps a trace of each. */
data class StrictTenderProfileEngine(
    var steps: MutableList<StrictTenderProfileModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictTenderProfile): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictTenderProfileModifier = steps[index]
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

    fun evaluateAll(items: MutableList<StrictTenderProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictTenderProfileEngine(flags: Registry): StrictTenderProfileEngine {
    return StrictTenderProfileEngine(
        steps = mutableListOf<StrictTenderProfileModifier>(
            RoundingStrictTenderProfileAdjuster(1.0),
            CapStrictTenderProfileAdjuster(30670.0),
            StrictTenderProfileCapacityOffsetAdjuster(1.51),
            MinimumStrictTenderProfileAdjuster(4.2),
            FlaggedStrictTenderProfileAdjuster(flags, "strict-tender-profile-rolling", -9.4),
            PercentStrictTenderProfileAdjuster(-15.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 67436.3,
    )
}
