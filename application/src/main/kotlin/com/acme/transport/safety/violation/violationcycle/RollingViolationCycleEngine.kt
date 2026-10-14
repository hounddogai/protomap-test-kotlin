package com.acme.transport.safety.violation.violationcycle

import com.acme.transport.kit.flags.Registry

/** One step of the rolling violation cycle calculation. */
interface RollingViolationCycleModifier {
    fun describe(): String
    fun adjust(item: RollingViolationCycle, value: Double): Double
}

data class TitleRollingViolationCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingViolationCycleModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: RollingViolationCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRollingViolationCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingViolationCycleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingViolationCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedRollingViolationCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingViolationCycleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingViolationCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the rolling violation cycle steps in order and keeps a trace of each. */
data class RollingViolationCycleEngine(
    var steps: MutableList<RollingViolationCycleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RollingViolationCycle): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingViolationCycleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RollingViolationCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRollingViolationCycleEngine(flags: Registry): RollingViolationCycleEngine {
    return RollingViolationCycleEngine(
        steps = mutableListOf<RollingViolationCycleModifier>(
            TitleRollingViolationCycleAdjuster(mutableMapOf<String, Double>("west" to 1.04)),
            TieredRollingViolationCycleAdjuster(
                mutableListOf<Double>(33.0, 5888.0, 6056.0, 6811.0),
                mutableListOf<Double>(0.88, 1.09, 0.91, 1.1),
            ),
            FlaggedRollingViolationCycleAdjuster(flags, "rolling-violation-cycle-pending", -7.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 77292.9,
    )
}
