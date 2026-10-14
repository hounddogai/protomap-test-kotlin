package com.acme.transport.linehaul.laneplan.window

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy lane plan bundle calculation. */
interface LegacyLanePlanBundleAdjuster {
    fun describe(): String
    fun adjust(item: LegacyLanePlanBundle, value: Double): Double
}

data class MinimumLegacyLanePlanBundleAdjuster(var minimum: Double) : LegacyLanePlanBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyLanePlanBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LegacyLanePlanBundleRatioOffsetAdjuster(var weight: Double) : LegacyLanePlanBundleAdjuster {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: LegacyLanePlanBundle, value: Double): Double = value + item.ratio * weight
}

data class PercentLegacyLanePlanBundleAdjuster(var percent: Double) : LegacyLanePlanBundleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyLanePlanBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLegacyLanePlanBundleAdjuster(var cap: Double) : LegacyLanePlanBundleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyLanePlanBundle, value: Double): Double = minOf(value, cap)
}

data class FlaggedLegacyLanePlanBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyLanePlanBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyLanePlanBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the legacy lane plan bundle steps in order and keeps a trace of each. */
data class LegacyLanePlanBundleEngine(
    var steps: MutableList<LegacyLanePlanBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyLanePlanBundle): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyLanePlanBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyLanePlanBundleEngine(flags: Registry): LegacyLanePlanBundleEngine {
    return LegacyLanePlanBundleEngine(
        steps = mutableListOf<LegacyLanePlanBundleAdjuster>(
            MinimumLegacyLanePlanBundleAdjuster(40.2),
            LegacyLanePlanBundleRatioOffsetAdjuster(0.74),
            PercentLegacyLanePlanBundleAdjuster(-13.7),
            CapLegacyLanePlanBundleAdjuster(31124.0),
            FlaggedLegacyLanePlanBundleAdjuster(flags, "legacy-lane-plan-bundle-pending", 3.9),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 57411.5,
    )
}
