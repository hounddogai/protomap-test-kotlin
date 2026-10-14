package com.acme.transport.customs.inspection.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the pending inspection baseline calculation. */
interface PendingInspectionBaselineFactor {
    fun describe(): String
    fun adjust(item: PendingInspectionBaseline, value: Double): Double
}

data class CapPendingInspectionBaselineAdjuster(var cap: Double) : PendingInspectionBaselineFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingInspectionBaseline, value: Double): Double = minOf(value, cap)
}

data class PercentPendingInspectionBaselineAdjuster(var percent: Double) : PendingInspectionBaselineFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingInspectionBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingPendingInspectionBaselineAdjuster(var step: Double) : PendingInspectionBaselineFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingInspectionBaseline, value: Double): Double = roundTo(value, step)
}

data class FlaggedPendingInspectionBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingInspectionBaselineFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingInspectionBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LocalePendingInspectionBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingInspectionBaselineFactor {
    override fun describe(): String = "by locale"

    override fun adjust(item: PendingInspectionBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending inspection baseline steps in order and keeps a trace of each. */
data class PendingInspectionBaselineEngine(
    var steps: MutableList<PendingInspectionBaselineFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingInspectionBaseline): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingInspectionBaselineFactor = steps[index]
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

    fun evaluateAll(items: MutableList<PendingInspectionBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingInspectionBaselineEngine(flags: Registry): PendingInspectionBaselineEngine {
    return PendingInspectionBaselineEngine(
        steps = mutableListOf<PendingInspectionBaselineFactor>(
            CapPendingInspectionBaselineAdjuster(28892.0),
            PercentPendingInspectionBaselineAdjuster(11.7),
            RoundingPendingInspectionBaselineAdjuster(0.5),
            FlaggedPendingInspectionBaselineAdjuster(flags, "pending-inspection-baseline-default", 5.9),
            LocalePendingInspectionBaselineAdjuster(
                mutableMapOf<String, Double>("central" to 0.91, "north" to 0.91, "retail" to 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 93190.3,
    )
}
