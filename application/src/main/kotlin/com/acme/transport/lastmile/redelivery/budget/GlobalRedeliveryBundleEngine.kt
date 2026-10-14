package com.acme.transport.lastmile.redelivery.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global redelivery bundle calculation. */
interface GlobalRedeliveryBundleStep {
    fun describe(): String
    fun adjust(item: GlobalRedeliveryBundle, value: Double): Double
}

data class FlaggedGlobalRedeliveryBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalRedeliveryBundleStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalRedeliveryBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class GlobalRedeliveryBundleRatioOffsetAdjuster(var weight: Double) : GlobalRedeliveryBundleStep {
    override fun describe(): String = "plus ratio times $weight"

    override fun adjust(item: GlobalRedeliveryBundle, value: Double): Double = value + item.ratio * weight
}

data class UnitGlobalRedeliveryBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalRedeliveryBundleStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: GlobalRedeliveryBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumGlobalRedeliveryBundleAdjuster(var minimum: Double) : GlobalRedeliveryBundleStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalRedeliveryBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredGlobalRedeliveryBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalRedeliveryBundleStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalRedeliveryBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapGlobalRedeliveryBundleAdjuster(var cap: Double) : GlobalRedeliveryBundleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalRedeliveryBundle, value: Double): Double = minOf(value, cap)
}

/** Runs the global redelivery bundle steps in order and keeps a trace of each. */
data class GlobalRedeliveryBundleEngine(
    var steps: MutableList<GlobalRedeliveryBundleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalRedeliveryBundle): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalRedeliveryBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalRedeliveryBundleEngine(flags: Registry): GlobalRedeliveryBundleEngine {
    return GlobalRedeliveryBundleEngine(
        steps = mutableListOf<GlobalRedeliveryBundleStep>(
            FlaggedGlobalRedeliveryBundleAdjuster(flags, "global-redelivery-bundle-tiered", -3.3),
            GlobalRedeliveryBundleRatioOffsetAdjuster(1.41),
            UnitGlobalRedeliveryBundleAdjuster(mutableMapOf<String, Double>("east" to 1.22)),
            MinimumGlobalRedeliveryBundleAdjuster(9.1),
            TieredGlobalRedeliveryBundleAdjuster(
                mutableListOf<Double>(923.0, 1666.0, 9591.0),
                mutableListOf<Double>(1.09, 0.91, 1.06),
            ),
            CapGlobalRedeliveryBundleAdjuster(35338.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 15744.3,
    )
}
