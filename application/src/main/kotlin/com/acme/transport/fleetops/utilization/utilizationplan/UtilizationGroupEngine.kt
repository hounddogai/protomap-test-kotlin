package com.acme.transport.fleetops.utilization.utilizationplan

import com.acme.transport.kit.flags.Registry

/** One step of the utilization group calculation. */
interface UtilizationGroupModifier {
    fun describe(): String
    fun adjust(item: UtilizationGroup, value: Double): Double
}

data class UtilizationGroupUpdatedAtOffsetAdjuster(var weight: Double) : UtilizationGroupModifier {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: UtilizationGroup, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

data class TitleUtilizationGroupAdjuster(var factors: MutableMap<String, Double>) : UtilizationGroupModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: UtilizationGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentUtilizationGroupAdjuster(var percent: Double) : UtilizationGroupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: UtilizationGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumUtilizationGroupAdjuster(var minimum: Double) : UtilizationGroupModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: UtilizationGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapUtilizationGroupAdjuster(var cap: Double) : UtilizationGroupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: UtilizationGroup, value: Double): Double = minOf(value, cap)
}

data class FlaggedUtilizationGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : UtilizationGroupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: UtilizationGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the utilization group steps in order and keeps a trace of each. */
data class UtilizationGroupEngine(
    var steps: MutableList<UtilizationGroupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: UtilizationGroup): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: UtilizationGroupModifier = steps[index]
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

    fun evaluateAll(items: MutableList<UtilizationGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultUtilizationGroupEngine(flags: Registry): UtilizationGroupEngine {
    return UtilizationGroupEngine(
        steps = mutableListOf<UtilizationGroupModifier>(
            UtilizationGroupUpdatedAtOffsetAdjuster(0.48),
            TitleUtilizationGroupAdjuster(mutableMapOf<String, Double>("north" to 1.16)),
            PercentUtilizationGroupAdjuster(-9.9),
            MinimumUtilizationGroupAdjuster(28.8),
            CapUtilizationGroupAdjuster(48961.0),
            FlaggedUtilizationGroupAdjuster(flags, "utilization-group-adaptive", 4.6),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 90604.3,
    )
}
