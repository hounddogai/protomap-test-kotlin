package com.acme.transport.fleetops.defect.defectproposal

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the fallback defect assignment calculation. */
interface FallbackDefectAssignmentStep {
    fun describe(): String
    fun adjust(item: FallbackDefectAssignment, value: Double): Double
}

data class FlaggedFallbackDefectAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackDefectAssignmentStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackDefectAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFallbackDefectAssignmentAdjuster(var minimum: Double) : FallbackDefectAssignmentStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackDefectAssignment, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingFallbackDefectAssignmentAdjuster(var step: Double) : FallbackDefectAssignmentStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackDefectAssignment, value: Double): Double = roundTo(value, step)
}

data class LaneFallbackDefectAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : FallbackDefectAssignmentStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: FallbackDefectAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentFallbackDefectAssignmentAdjuster(var percent: Double) : FallbackDefectAssignmentStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackDefectAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fallback defect assignment steps in order and keeps a trace of each. */
data class FallbackDefectAssignmentEngine(
    var steps: MutableList<FallbackDefectAssignmentStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackDefectAssignment): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: FallbackDefectAssignmentStep = steps[index]
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

    fun evaluateAll(items: MutableList<FallbackDefectAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackDefectAssignmentEngine(flags: Registry): FallbackDefectAssignmentEngine {
    return FallbackDefectAssignmentEngine(
        steps = mutableListOf<FallbackDefectAssignmentStep>(
            FlaggedFallbackDefectAssignmentAdjuster(flags, "fallback-defect-assignment-manual", 0.7),
            MinimumFallbackDefectAssignmentAdjuster(35.5),
            RoundingFallbackDefectAssignmentAdjuster(0.5),
            LaneFallbackDefectAssignmentAdjuster(
                mutableMapOf<String, Double>("online" to 0.87, "north" to 0.94),
            ),
            PercentFallbackDefectAssignmentAdjuster(-18.3),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 75038.1,
    )
}
