package com.acme.transport.customs.clearance.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback clearance assignment calculation. */
interface FallbackClearanceAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: FallbackClearanceAssignment, value: Double): Double
}

data class RoundingFallbackClearanceAssignmentAdjuster(var step: Double) : FallbackClearanceAssignmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackClearanceAssignment, value: Double): Double = roundTo(value, step)
}

data class CapFallbackClearanceAssignmentAdjuster(var cap: Double) : FallbackClearanceAssignmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackClearanceAssignment, value: Double): Double = minOf(value, cap)
}

data class FlaggedFallbackClearanceAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackClearanceAssignmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackClearanceAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFallbackClearanceAssignmentAdjuster(
    var minimum: Double,
) : FallbackClearanceAssignmentAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackClearanceAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelFallbackClearanceAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackClearanceAssignmentAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: FallbackClearanceAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the fallback clearance assignment steps in order and keeps a trace of each. */
data class FallbackClearanceAssignmentEngine(
    var steps: MutableList<FallbackClearanceAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackClearanceAssignment): Double {
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

    fun evaluateAll(items: MutableList<FallbackClearanceAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackClearanceAssignmentEngine(flags: Registry): FallbackClearanceAssignmentEngine {
    return FallbackClearanceAssignmentEngine(
        steps = mutableListOf<FallbackClearanceAssignmentAdjuster>(
            RoundingFallbackClearanceAssignmentAdjuster(0.05),
            CapFallbackClearanceAssignmentAdjuster(6279.0),
            FlaggedFallbackClearanceAssignmentAdjuster(flags, "fallback-clearance-assignment-primary", 1.6),
            MinimumFallbackClearanceAssignmentAdjuster(3.0),
            LabelFallbackClearanceAssignmentAdjuster(mutableMapOf<String, Double>("online" to 0.78)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 41988.3,
    )
}
