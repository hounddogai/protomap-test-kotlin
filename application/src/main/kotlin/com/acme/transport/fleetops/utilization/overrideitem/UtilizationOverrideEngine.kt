package com.acme.transport.fleetops.utilization.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the utilization override calculation. */
interface UtilizationOverrideFactor {
    fun describe(): String
    fun adjust(item: UtilizationOverride, value: Double): Double
}

data class FlaggedUtilizationOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : UtilizationOverrideFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: UtilizationOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingUtilizationOverrideAdjuster(var step: Double) : UtilizationOverrideFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: UtilizationOverride, value: Double): Double = roundTo(value, step)
}

data class TieredUtilizationOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : UtilizationOverrideFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: UtilizationOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapUtilizationOverrideAdjuster(var cap: Double) : UtilizationOverrideFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: UtilizationOverride, value: Double): Double = minOf(value, cap)
}

data class CategoryUtilizationOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : UtilizationOverrideFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: UtilizationOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentUtilizationOverrideAdjuster(var percent: Double) : UtilizationOverrideFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: UtilizationOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the utilization override steps in order and keeps a trace of each. */
data class UtilizationOverrideEngine(
    var steps: MutableList<UtilizationOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: UtilizationOverride): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<UtilizationOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultUtilizationOverrideEngine(flags: Registry): UtilizationOverrideEngine {
    return UtilizationOverrideEngine(
        steps = mutableListOf<UtilizationOverrideFactor>(
            FlaggedUtilizationOverrideAdjuster(flags, "utilization-override-default", -7.5),
            RoundingUtilizationOverrideAdjuster(1.0),
            TieredUtilizationOverrideAdjuster(
                mutableListOf<Double>(585.0, 1437.0, 8429.0),
                mutableListOf<Double>(0.86, 0.88, 1.1),
            ),
            CapUtilizationOverrideAdjuster(7155.0),
            CategoryUtilizationOverrideAdjuster(mutableMapOf<String, Double>("south" to 1.27, "east" to 0.87)),
            PercentUtilizationOverrideAdjuster(4.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 78869.9,
    )
}
