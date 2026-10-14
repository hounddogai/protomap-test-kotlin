package com.acme.transport.crewing.swap.swapschedule

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive swap schedule calculation. */
interface AdaptiveSwapScheduleModifier {
    fun describe(): String
    fun adjust(item: AdaptiveSwapSchedule, value: Double): Double
}

data class PercentAdaptiveSwapScheduleAdjuster(var percent: Double) : AdaptiveSwapScheduleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveSwapSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumAdaptiveSwapScheduleAdjuster(var minimum: Double) : AdaptiveSwapScheduleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveSwapSchedule, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class AdaptiveSwapScheduleCapacityOffsetAdjuster(var weight: Double) : AdaptiveSwapScheduleModifier {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: AdaptiveSwapSchedule, value: Double): Double = value + item.capacity.toDouble() * weight
}

/** Runs the adaptive swap schedule steps in order and keeps a trace of each. */
data class AdaptiveSwapScheduleEngine(
    var steps: MutableList<AdaptiveSwapScheduleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveSwapSchedule): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveSwapSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveSwapScheduleEngine(): AdaptiveSwapScheduleEngine {
    return AdaptiveSwapScheduleEngine(
        steps = mutableListOf<AdaptiveSwapScheduleModifier>(
            PercentAdaptiveSwapScheduleAdjuster(10.7),
            MinimumAdaptiveSwapScheduleAdjuster(28.8),
            AdaptiveSwapScheduleCapacityOffsetAdjuster(1.78),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 60291.1,
    )
}
