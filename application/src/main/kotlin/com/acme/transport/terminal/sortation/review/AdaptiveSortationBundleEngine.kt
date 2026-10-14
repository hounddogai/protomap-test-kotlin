package com.acme.transport.terminal.sortation.review

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the adaptive sortation bundle calculation. */
interface AdaptiveSortationBundleAdjuster {
    fun describe(): String
    fun adjust(item: AdaptiveSortationBundle, value: Double): Double
}

data class MinimumAdaptiveSortationBundleAdjuster(var minimum: Double) : AdaptiveSortationBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveSortationBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapAdaptiveSortationBundleAdjuster(var cap: Double) : AdaptiveSortationBundleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveSortationBundle, value: Double): Double = minOf(value, cap)
}

data class ChannelAdaptiveSortationBundleAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveSortationBundleAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: AdaptiveSortationBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive sortation bundle steps in order and keeps a trace of each. */
data class AdaptiveSortationBundleEngine(
    var steps: MutableList<AdaptiveSortationBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveSortationBundle): Double {
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

    fun evaluateAll(items: MutableList<AdaptiveSortationBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveSortationBundleEngine(): AdaptiveSortationBundleEngine {
    return AdaptiveSortationBundleEngine(
        steps = mutableListOf<AdaptiveSortationBundleAdjuster>(
            MinimumAdaptiveSortationBundleAdjuster(18.7),
            CapAdaptiveSortationBundleAdjuster(17732.0),
            ChannelAdaptiveSortationBundleAdjuster(
                mutableMapOf<String, Double>("online" to 1.0, "retail" to 1.29),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 32188.9,
    )
}
