package com.acme.transport.fleetops.utilization.utilizationreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the utilization profile calculation. */
interface UtilizationProfileAdjuster {
    fun describe(): String
    fun adjust(item: UtilizationProfile, value: Double): Double
}

data class FlaggedUtilizationProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : UtilizationProfileAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: UtilizationProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class UtilizationProfileDiscountOffsetAdjuster(var weight: Double) : UtilizationProfileAdjuster {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: UtilizationProfile, value: Double): Double = value + item.discount * weight
}

data class TieredUtilizationProfileAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : UtilizationProfileAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: UtilizationProfile, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the utilization profile steps in order and keeps a trace of each. */
data class UtilizationProfileEngine(
    var steps: MutableList<UtilizationProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: UtilizationProfile): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<UtilizationProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultUtilizationProfileEngine(flags: Registry): UtilizationProfileEngine {
    return UtilizationProfileEngine(
        steps = mutableListOf<UtilizationProfileAdjuster>(
            FlaggedUtilizationProfileAdjuster(flags, "utilization-profile-fallback", -7.2),
            UtilizationProfileDiscountOffsetAdjuster(0.41),
            TieredUtilizationProfileAdjuster(
                mutableListOf<Double>(1302.0, 7076.0, 7942.0),
                mutableListOf<Double>(0.96, 0.87, 0.8),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 99451.4,
    )
}
