package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.flags.Registry

/** One step of the regional lane plan override calculation. */
interface RegionalLanePlanOverrideStep {
    fun describe(): String
    fun adjust(item: RegionalLanePlanOverride, value: Double): Double
}

data class MinimumRegionalLanePlanOverrideAdjuster(var minimum: Double) : RegionalLanePlanOverrideStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalLanePlanOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class NameRegionalLanePlanOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalLanePlanOverrideStep {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalLanePlanOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRegionalLanePlanOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalLanePlanOverrideStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalLanePlanOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalLanePlanOverrideAdjuster(var cap: Double) : RegionalLanePlanOverrideStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalLanePlanOverride, value: Double): Double = minOf(value, cap)
}

data class RegionalLanePlanOverrideWeightOffsetAdjuster(var weight: Double) : RegionalLanePlanOverrideStep {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: RegionalLanePlanOverride, value: Double): Double = value + item.weight * weight
}

data class PercentRegionalLanePlanOverrideAdjuster(var percent: Double) : RegionalLanePlanOverrideStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalLanePlanOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional lane plan override steps in order and keeps a trace of each. */
data class RegionalLanePlanOverrideEngine(
    var steps: MutableList<RegionalLanePlanOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalLanePlanOverride): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalLanePlanOverrideStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalLanePlanOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalLanePlanOverrideEngine(flags: Registry): RegionalLanePlanOverrideEngine {
    return RegionalLanePlanOverrideEngine(
        steps = mutableListOf<RegionalLanePlanOverrideStep>(
            MinimumRegionalLanePlanOverrideAdjuster(27.9),
            NameRegionalLanePlanOverrideAdjuster(
                mutableMapOf<String, Double>("central" to 1.28, "north" to 0.8),
            ),
            FlaggedRegionalLanePlanOverrideAdjuster(flags, "regional-lane-plan-override-scheduled", 8.0),
            CapRegionalLanePlanOverrideAdjuster(36269.0),
            RegionalLanePlanOverrideWeightOffsetAdjuster(0.97),
            PercentRegionalLanePlanOverrideAdjuster(-19.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 1582.9,
    )
}
