package com.acme.transport.linehaul.laneplan.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback lane plan bundle calculation. */
interface FallbackLanePlanBundleAdjuster {
    fun describe(): String
    fun adjust(item: FallbackLanePlanBundle, value: Double): Double
}

data class FlaggedFallbackLanePlanBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackLanePlanBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackLanePlanBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumFallbackLanePlanBundleAdjuster(var minimum: Double) : FallbackLanePlanBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackLanePlanBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredFallbackLanePlanBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackLanePlanBundleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackLanePlanBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentFallbackLanePlanBundleAdjuster(var percent: Double) : FallbackLanePlanBundleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackLanePlanBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the fallback lane plan bundle steps in order and keeps a trace of each. */
data class FallbackLanePlanBundleEngine(
    var steps: MutableList<FallbackLanePlanBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: FallbackLanePlanBundle): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackLanePlanBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultFallbackLanePlanBundleEngine(flags: Registry): FallbackLanePlanBundleEngine {
    return FallbackLanePlanBundleEngine(
        steps = mutableListOf<FallbackLanePlanBundleAdjuster>(
            FlaggedFallbackLanePlanBundleAdjuster(flags, "fallback-lane-plan-bundle-rolling", -8.6),
            MinimumFallbackLanePlanBundleAdjuster(20.3),
            TieredFallbackLanePlanBundleAdjuster(
                mutableListOf<Double>(5872.0, 6416.0, 7865.0),
                mutableListOf<Double>(1.14, 1.02, 1.18),
            ),
            PercentFallbackLanePlanBundleAdjuster(20.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 69412.4,
    )
}
