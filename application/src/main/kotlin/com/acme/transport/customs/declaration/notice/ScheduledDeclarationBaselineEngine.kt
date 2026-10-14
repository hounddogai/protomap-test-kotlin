package com.acme.transport.customs.declaration.notice

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled declaration baseline calculation. */
interface ScheduledDeclarationBaselineAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledDeclarationBaseline, value: Double): Double
}

data class ScheduledDeclarationBaselineWeightOffsetAdjuster(
    var weight: Double,
) : ScheduledDeclarationBaselineAdjuster {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: ScheduledDeclarationBaseline, value: Double): Double = value + item.weight * weight
}

data class FlaggedScheduledDeclarationBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledDeclarationBaselineAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledDeclarationBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryScheduledDeclarationBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledDeclarationBaselineAdjuster {
    override fun describe(): String = "by category"

    override fun adjust(item: ScheduledDeclarationBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled declaration baseline steps in order and keeps a trace of each. */
data class ScheduledDeclarationBaselineEngine(
    var steps: MutableList<ScheduledDeclarationBaselineAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledDeclarationBaseline): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledDeclarationBaselineAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledDeclarationBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledDeclarationBaselineEngine(flags: Registry): ScheduledDeclarationBaselineEngine {
    return ScheduledDeclarationBaselineEngine(
        steps = mutableListOf<ScheduledDeclarationBaselineAdjuster>(
            ScheduledDeclarationBaselineWeightOffsetAdjuster(0.57),
            FlaggedScheduledDeclarationBaselineAdjuster(flags, "scheduled-declaration-baseline-regional", 9.6),
            CategoryScheduledDeclarationBaselineAdjuster(mutableMapOf<String, Double>("central" to 0.99)),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 74150.7,
    )
}
