package com.acme.transport.linehaul.capacity.capacitycycle

import com.acme.transport.kit.flags.Registry

/** One step of the default capacity target calculation. */
interface DefaultCapacityTargetModifier {
    fun describe(): String
    fun adjust(item: DefaultCapacityTarget, value: Double): Double
}

data class MinimumDefaultCapacityTargetAdjuster(var minimum: Double) : DefaultCapacityTargetModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultCapacityTarget, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapDefaultCapacityTargetAdjuster(var cap: Double) : DefaultCapacityTargetModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultCapacityTarget, value: Double): Double = minOf(value, cap)
}

data class PercentDefaultCapacityTargetAdjuster(var percent: Double) : DefaultCapacityTargetModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultCapacityTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedDefaultCapacityTargetAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultCapacityTargetModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultCapacityTarget, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SourceDefaultCapacityTargetAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultCapacityTargetModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: DefaultCapacityTarget, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the default capacity target steps in order and keeps a trace of each. */
data class DefaultCapacityTargetEngine(
    var steps: MutableList<DefaultCapacityTargetModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultCapacityTarget): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultCapacityTargetModifier = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultCapacityTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultCapacityTargetEngine(flags: Registry): DefaultCapacityTargetEngine {
    return DefaultCapacityTargetEngine(
        steps = mutableListOf<DefaultCapacityTargetModifier>(
            MinimumDefaultCapacityTargetAdjuster(30.6),
            CapDefaultCapacityTargetAdjuster(28268.0),
            PercentDefaultCapacityTargetAdjuster(7.5),
            FlaggedDefaultCapacityTargetAdjuster(flags, "default-capacity-target-pending", 5.7),
            SourceDefaultCapacityTargetAdjuster(
                mutableMapOf<String, Double>("central" to 1.01, "retail" to 0.91),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 53341.6,
    )
}
