package com.acme.transport.fleetops.inspection.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the manual inspection override calculation. */
interface ManualInspectionOverrideFactor {
    fun describe(): String
    fun adjust(item: ManualInspectionOverride, value: Double): Double
}

data class RoundingManualInspectionOverrideAdjuster(var step: Double) : ManualInspectionOverrideFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualInspectionOverride, value: Double): Double = roundTo(value, step)
}

data class FlaggedManualInspectionOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualInspectionOverrideFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualInspectionOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumManualInspectionOverrideAdjuster(var minimum: Double) : ManualInspectionOverrideFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualInspectionOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameManualInspectionOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualInspectionOverrideFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: ManualInspectionOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapManualInspectionOverrideAdjuster(var cap: Double) : ManualInspectionOverrideFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualInspectionOverride, value: Double): Double = minOf(value, cap)
}

/** Runs the manual inspection override steps in order and keeps a trace of each. */
data class ManualInspectionOverrideEngine(
    var steps: MutableList<ManualInspectionOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualInspectionOverride): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualInspectionOverrideFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ManualInspectionOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualInspectionOverrideEngine(flags: Registry): ManualInspectionOverrideEngine {
    return ManualInspectionOverrideEngine(
        steps = mutableListOf<ManualInspectionOverrideFactor>(
            RoundingManualInspectionOverrideAdjuster(0.25),
            FlaggedManualInspectionOverrideAdjuster(flags, "manual-inspection-override-batched", -8.9),
            MinimumManualInspectionOverrideAdjuster(31.6),
            NameManualInspectionOverrideAdjuster(mutableMapOf<String, Double>("online" to 1.16)),
            CapManualInspectionOverrideAdjuster(2496.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.4,
        ceiling = 92824.5,
    )
}
