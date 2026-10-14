package com.acme.transport.safety.scorecard.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending scorecard assignment calculation. */
interface PendingScorecardAssignmentAdjuster {
    fun describe(): String
    fun adjust(item: PendingScorecardAssignment, value: Double): Double
}

data class CapPendingScorecardAssignmentAdjuster(var cap: Double) : PendingScorecardAssignmentAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingScorecardAssignment, value: Double): Double = minOf(value, cap)
}

data class FlaggedPendingScorecardAssignmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingScorecardAssignmentAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingScorecardAssignment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingPendingScorecardAssignmentAdjuster(var step: Double) : PendingScorecardAssignmentAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingScorecardAssignment, value: Double): Double = roundTo(value, step)
}

data class PercentPendingScorecardAssignmentAdjuster(var percent: Double) : PendingScorecardAssignmentAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingScorecardAssignment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredPendingScorecardAssignmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingScorecardAssignmentAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingScorecardAssignment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class TitlePendingScorecardAssignmentAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingScorecardAssignmentAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: PendingScorecardAssignment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending scorecard assignment steps in order and keeps a trace of each. */
data class PendingScorecardAssignmentEngine(
    var steps: MutableList<PendingScorecardAssignmentAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingScorecardAssignment): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingScorecardAssignmentAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<PendingScorecardAssignment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingScorecardAssignmentEngine(flags: Registry): PendingScorecardAssignmentEngine {
    return PendingScorecardAssignmentEngine(
        steps = mutableListOf<PendingScorecardAssignmentAdjuster>(
            CapPendingScorecardAssignmentAdjuster(32099.0),
            FlaggedPendingScorecardAssignmentAdjuster(flags, "pending-scorecard-assignment-primary", -1.7),
            RoundingPendingScorecardAssignmentAdjuster(0.5),
            PercentPendingScorecardAssignmentAdjuster(-11.9),
            TieredPendingScorecardAssignmentAdjuster(
                mutableListOf<Double>(6370.0, 7432.0, 7728.0, 8081.0),
                mutableListOf<Double>(0.85, 1.1, 0.82, 0.81),
            ),
            TitlePendingScorecardAssignmentAdjuster(
                mutableMapOf<String, Double>("retail" to 1.37, "north" to 1.06, "west" to 1.02),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 53339.1,
    )
}
