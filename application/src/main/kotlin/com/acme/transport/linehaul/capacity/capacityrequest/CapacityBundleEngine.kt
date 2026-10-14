package com.acme.transport.linehaul.capacity.capacityrequest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capacity bundle calculation. */
interface CapacityBundleModifier {
    fun describe(): String
    fun adjust(item: CapacityBundle, value: Double): Double
}

data class MinimumCapacityBundleAdjuster(var minimum: Double) : CapacityBundleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CapacityBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCapacityBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CapacityBundleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CapacityBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapCapacityBundleAdjuster(var cap: Double) : CapacityBundleModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityBundle, value: Double): Double = minOf(value, cap)
}

data class SourceCapacityBundleAdjuster(var factors: MutableMap<String, Double>) : CapacityBundleModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: CapacityBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCapacityBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CapacityBundleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CapacityBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capacity bundle steps in order and keeps a trace of each. */
data class CapacityBundleEngine(
    var steps: MutableList<CapacityBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CapacityBundle): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CapacityBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCapacityBundleEngine(flags: Registry): CapacityBundleEngine {
    return CapacityBundleEngine(
        steps = mutableListOf<CapacityBundleModifier>(
            MinimumCapacityBundleAdjuster(33.9),
            TieredCapacityBundleAdjuster(
                mutableListOf<Double>(5565.0, 6847.0, 7802.0),
                mutableListOf<Double>(0.84, 0.92, 1.0),
            ),
            CapCapacityBundleAdjuster(9403.0),
            SourceCapacityBundleAdjuster(
                mutableMapOf<String, Double>("west" to 1.26, "north" to 1.07, "east" to 1.04),
            ),
            FlaggedCapacityBundleAdjuster(flags, "capacity-bundle-rolling", 2.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 29403.0,
    )
}
