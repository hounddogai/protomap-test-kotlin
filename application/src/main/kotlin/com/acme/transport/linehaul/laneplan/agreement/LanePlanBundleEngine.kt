package com.acme.transport.linehaul.laneplan.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the lane plan bundle calculation. */
interface LanePlanBundleAdjuster {
    fun describe(): String
    fun adjust(item: LanePlanBundle, value: Double): Double
}

data class TieredLanePlanBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LanePlanBundleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LanePlanBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLanePlanBundleAdjuster(var percent: Double) : LanePlanBundleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LanePlanBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLanePlanBundleAdjuster(var cap: Double) : LanePlanBundleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LanePlanBundle, value: Double): Double = minOf(value, cap)
}

data class NameLanePlanBundleAdjuster(var factors: MutableMap<String, Double>) : LanePlanBundleAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: LanePlanBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LanePlanBundleLimitOffsetAdjuster(var weight: Double) : LanePlanBundleAdjuster {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: LanePlanBundle, value: Double): Double = value + item.limit.toDouble() * weight
}

data class FlaggedLanePlanBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LanePlanBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LanePlanBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the lane plan bundle steps in order and keeps a trace of each. */
data class LanePlanBundleEngine(
    var steps: MutableList<LanePlanBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LanePlanBundle): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LanePlanBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLanePlanBundleEngine(flags: Registry): LanePlanBundleEngine {
    return LanePlanBundleEngine(
        steps = mutableListOf<LanePlanBundleAdjuster>(
            TieredLanePlanBundleAdjuster(
                mutableListOf<Double>(927.0, 1740.0, 3266.0, 3799.0),
                mutableListOf<Double>(0.8, 1.07, 1.15, 0.82),
            ),
            PercentLanePlanBundleAdjuster(-1.9),
            CapLanePlanBundleAdjuster(49930.0),
            NameLanePlanBundleAdjuster(
                mutableMapOf<String, Double>("south" to 1.27, "retail" to 1.22, "central" to 0.9),
            ),
            LanePlanBundleLimitOffsetAdjuster(1.22),
            FlaggedLanePlanBundleAdjuster(flags, "lane-plan-bundle-default", -1.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 35319.2,
    )
}
