package com.acme.transport.customs.inspection.inspectionoverride

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled inspection claim calculation. */
interface ScheduledInspectionClaimStep {
    fun describe(): String
    fun adjust(item: ScheduledInspectionClaim, value: Double): Double
}

data class RoundingScheduledInspectionClaimAdjuster(var step: Double) : ScheduledInspectionClaimStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledInspectionClaim, value: Double): Double = roundTo(value, step)
}

data class ScheduledInspectionClaimMaximumOffsetAdjuster(var weight: Double) : ScheduledInspectionClaimStep {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: ScheduledInspectionClaim, value: Double): Double = value + item.maximum * weight
}

data class FlaggedScheduledInspectionClaimAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledInspectionClaimStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledInspectionClaim, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumScheduledInspectionClaimAdjuster(var minimum: Double) : ScheduledInspectionClaimStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledInspectionClaim, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LaneScheduledInspectionClaimAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledInspectionClaimStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: ScheduledInspectionClaim, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentScheduledInspectionClaimAdjuster(var percent: Double) : ScheduledInspectionClaimStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledInspectionClaim, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scheduled inspection claim steps in order and keeps a trace of each. */
data class ScheduledInspectionClaimEngine(
    var steps: MutableList<ScheduledInspectionClaimStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledInspectionClaim): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledInspectionClaimStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledInspectionClaim>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledInspectionClaimEngine(flags: Registry): ScheduledInspectionClaimEngine {
    return ScheduledInspectionClaimEngine(
        steps = mutableListOf<ScheduledInspectionClaimStep>(
            RoundingScheduledInspectionClaimAdjuster(5.0),
            ScheduledInspectionClaimMaximumOffsetAdjuster(0.61),
            FlaggedScheduledInspectionClaimAdjuster(flags, "scheduled-inspection-claim-legacy", -9.7),
            MinimumScheduledInspectionClaimAdjuster(31.5),
            LaneScheduledInspectionClaimAdjuster(
                mutableMapOf<String, Double>("west" to 1.32, "south" to 1.29, "central" to 1.14),
            ),
            PercentScheduledInspectionClaimAdjuster(9.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 24535.0,
    )
}
