package com.acme.transport.network.zone.overrideitem

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global zone profile calculation. */
interface GlobalZoneProfileAdjuster {
    fun describe(): String
    fun adjust(item: GlobalZoneProfile, value: Double): Double
}

data class SegmentGlobalZoneProfileAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalZoneProfileAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: GlobalZoneProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class GlobalZoneProfileThresholdOffsetAdjuster(var weight: Double) : GlobalZoneProfileAdjuster {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: GlobalZoneProfile, value: Double): Double = value + item.threshold * weight
}

data class TieredGlobalZoneProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalZoneProfileAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalZoneProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumGlobalZoneProfileAdjuster(var minimum: Double) : GlobalZoneProfileAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalZoneProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapGlobalZoneProfileAdjuster(var cap: Double) : GlobalZoneProfileAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalZoneProfile, value: Double): Double = minOf(value, cap)
}

data class PercentGlobalZoneProfileAdjuster(var percent: Double) : GlobalZoneProfileAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalZoneProfile, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the global zone profile steps in order and keeps a trace of each. */
data class GlobalZoneProfileEngine(
    var steps: MutableList<GlobalZoneProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalZoneProfile): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalZoneProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalZoneProfileEngine(): GlobalZoneProfileEngine {
    return GlobalZoneProfileEngine(
        steps = mutableListOf<GlobalZoneProfileAdjuster>(
            SegmentGlobalZoneProfileAdjuster(mutableMapOf<String, Double>("west" to 0.71, "north" to 1.23)),
            GlobalZoneProfileThresholdOffsetAdjuster(0.15),
            TieredGlobalZoneProfileAdjuster(
                mutableListOf<Double>(1877.0, 2468.0, 6361.0, 9732.0),
                mutableListOf<Double>(1.04, 1.06, 1.01, 1.0),
            ),
            MinimumGlobalZoneProfileAdjuster(23.3),
            CapGlobalZoneProfileAdjuster(39436.0),
            PercentGlobalZoneProfileAdjuster(6.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 72586.7,
    )
}
