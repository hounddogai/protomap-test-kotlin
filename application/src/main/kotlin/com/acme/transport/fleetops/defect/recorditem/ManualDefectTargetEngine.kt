package com.acme.transport.fleetops.defect.recorditem

import com.acme.transport.kit.flags.Registry

/** One step of the manual defect target calculation. */
interface ManualDefectTargetAdjuster {
    fun describe(): String
    fun adjust(item: ManualDefectTarget, value: Double): Double
}

data class ManualDefectTargetScoreOffsetAdjuster(var weight: Double) : ManualDefectTargetAdjuster {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: ManualDefectTarget, value: Double): Double = value + item.score * weight
}

data class MinimumManualDefectTargetAdjuster(var minimum: Double) : ManualDefectTargetAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualDefectTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredManualDefectTargetAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualDefectTargetAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualDefectTarget, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedManualDefectTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualDefectTargetAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualDefectTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelManualDefectTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualDefectTargetAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: ManualDefectTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual defect target steps in order and keeps a trace of each. */
data class ManualDefectTargetEngine(
    var steps: MutableList<ManualDefectTargetAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualDefectTarget): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualDefectTargetAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ManualDefectTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualDefectTargetEngine(flags: Registry): ManualDefectTargetEngine {
    return ManualDefectTargetEngine(
        steps = mutableListOf<ManualDefectTargetAdjuster>(
            ManualDefectTargetScoreOffsetAdjuster(1.02),
            MinimumManualDefectTargetAdjuster(2.1),
            TieredManualDefectTargetAdjuster(
                mutableListOf<Double>(7246.0, 8241.0, 9232.0),
                mutableListOf<Double>(1.16, 0.89, 1.01),
            ),
            FlaggedManualDefectTargetAdjuster(flags, "manual-defect-target-pending", -6.6),
            LabelManualDefectTargetAdjuster(
                mutableMapOf<String, Double>("online" to 0.91, "retail" to 0.82, "north" to 1.19),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 99286.0,
    )
}
