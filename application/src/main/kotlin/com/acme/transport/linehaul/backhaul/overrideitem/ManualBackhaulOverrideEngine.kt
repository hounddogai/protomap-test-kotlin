package com.acme.transport.linehaul.backhaul.overrideitem

import com.acme.transport.kit.flags.Registry

/** One step of the manual backhaul override calculation. */
interface ManualBackhaulOverrideFactor {
    fun describe(): String
    fun adjust(item: ManualBackhaulOverride, value: Double): Double
}

data class ManualBackhaulOverrideCapacityOffsetAdjuster(var weight: Double) : ManualBackhaulOverrideFactor {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: ManualBackhaulOverride, value: Double): Double {
        return value + item.capacity.toDouble() * weight
    }
}

data class PercentManualBackhaulOverrideAdjuster(var percent: Double) : ManualBackhaulOverrideFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualBackhaulOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapManualBackhaulOverrideAdjuster(var cap: Double) : ManualBackhaulOverrideFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualBackhaulOverride, value: Double): Double = minOf(value, cap)
}

data class FlaggedManualBackhaulOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualBackhaulOverrideFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualBackhaulOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumManualBackhaulOverrideAdjuster(var minimum: Double) : ManualBackhaulOverrideFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualBackhaulOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredManualBackhaulOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualBackhaulOverrideFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualBackhaulOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual backhaul override steps in order and keeps a trace of each. */
data class ManualBackhaulOverrideEngine(
    var steps: MutableList<ManualBackhaulOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ManualBackhaulOverride): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ManualBackhaulOverrideFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ManualBackhaulOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultManualBackhaulOverrideEngine(flags: Registry): ManualBackhaulOverrideEngine {
    return ManualBackhaulOverrideEngine(
        steps = mutableListOf<ManualBackhaulOverrideFactor>(
            ManualBackhaulOverrideCapacityOffsetAdjuster(1.52),
            PercentManualBackhaulOverrideAdjuster(-12.4),
            CapManualBackhaulOverrideAdjuster(13901.0),
            FlaggedManualBackhaulOverrideAdjuster(flags, "manual-backhaul-override-weighted", 6.9),
            MinimumManualBackhaulOverrideAdjuster(43.5),
            TieredManualBackhaulOverrideAdjuster(
                mutableListOf<Double>(1041.0, 6173.0, 7082.0),
                mutableListOf<Double>(1.15, 0.85, 1.12),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 4328.1,
    )
}
