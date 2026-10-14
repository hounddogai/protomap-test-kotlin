package com.acme.transport.customs.clearance.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the clearance assignment calculation. */
interface ClearanceAssignmentModifier {
    fun describe(): String
    fun adjust(item: ClearanceAssignment, value: Double): Double
}

data class RoundingClearanceAssignmentAdjuster(var step: Double) : ClearanceAssignmentModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ClearanceAssignment, value: Double): Double = roundTo(value, step)
}

data class TitleClearanceAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : ClearanceAssignmentModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: ClearanceAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapClearanceAssignmentAdjuster(var cap: Double) : ClearanceAssignmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ClearanceAssignment, value: Double): Double = minOf(value, cap)
}

data class TieredClearanceAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceAssignmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumClearanceAssignmentAdjuster(var minimum: Double) : ClearanceAssignmentModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ClearanceAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedClearanceAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ClearanceAssignmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ClearanceAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the clearance assignment steps in order and keeps a trace of each. */
data class ClearanceAssignmentEngine(
    var steps: MutableList<ClearanceAssignmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ClearanceAssignment): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearanceAssignmentModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ClearanceAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultClearanceAssignmentEngine(flags: Registry): ClearanceAssignmentEngine {
    return ClearanceAssignmentEngine(
        steps = mutableListOf<ClearanceAssignmentModifier>(
            RoundingClearanceAssignmentAdjuster(0.25),
            TitleClearanceAssignmentAdjuster(mutableMapOf<String, Double>("central" to 0.86, "south" to 1.08)),
            CapClearanceAssignmentAdjuster(35432.0),
            TieredClearanceAssignmentAdjuster(
                mutableListOf<Double>(4939.0, 6959.0, 7466.0, 7733.0),
                mutableListOf<Double>(0.82, 0.85, 1.17, 0.84),
            ),
            MinimumClearanceAssignmentAdjuster(29.5),
            FlaggedClearanceAssignmentAdjuster(flags, "clearance-assignment-rolling", 7.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 47943.9,
    )
}
