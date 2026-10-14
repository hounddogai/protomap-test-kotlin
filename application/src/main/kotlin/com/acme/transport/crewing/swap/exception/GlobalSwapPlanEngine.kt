package com.acme.transport.crewing.swap.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global swap plan calculation. */
interface GlobalSwapPlanAdjuster {
    fun describe(): String
    fun adjust(item: GlobalSwapPlan, value: Double): Double
}

data class GlobalSwapPlanRetriesOffsetAdjuster(var weight: Double) : GlobalSwapPlanAdjuster {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: GlobalSwapPlan, value: Double): Double = value + item.retries.toDouble() * weight
}

data class FlaggedGlobalSwapPlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalSwapPlanAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalSwapPlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredGlobalSwapPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalSwapPlanAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalSwapPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class SegmentGlobalSwapPlanAdjuster(var factors: MutableMap<String, Double>) : GlobalSwapPlanAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: GlobalSwapPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the global swap plan steps in order and keeps a trace of each. */
data class GlobalSwapPlanEngine(
    var steps: MutableList<GlobalSwapPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalSwapPlan): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalSwapPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalSwapPlanEngine(flags: Registry): GlobalSwapPlanEngine {
    return GlobalSwapPlanEngine(
        steps = mutableListOf<GlobalSwapPlanAdjuster>(
            GlobalSwapPlanRetriesOffsetAdjuster(0.21),
            FlaggedGlobalSwapPlanAdjuster(flags, "global-swap-plan-manual", 9.8),
            TieredGlobalSwapPlanAdjuster(
                mutableListOf<Double>(1735.0, 7462.0),
                mutableListOf<Double>(1.01, 1.12),
            ),
            SegmentGlobalSwapPlanAdjuster(
                mutableMapOf<String, Double>("east" to 1.4, "west" to 0.91, "central" to 0.81),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 64568.8,
    )
}
