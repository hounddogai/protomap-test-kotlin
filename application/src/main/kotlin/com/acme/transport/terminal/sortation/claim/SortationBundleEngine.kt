package com.acme.transport.terminal.sortation.claim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the sortation bundle calculation. */
interface SortationBundleModifier {
    fun describe(): String
    fun adjust(item: SortationBundle, value: Double): Double
}

data class PercentSortationBundleAdjuster(var percent: Double) : SortationBundleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: SortationBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleSortationBundleAdjuster(var factors: MutableMap<String, Double>) : SortationBundleModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: SortationBundle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredSortationBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : SortationBundleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: SortationBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedSortationBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : SortationBundleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: SortationBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingSortationBundleAdjuster(var step: Double) : SortationBundleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: SortationBundle, value: Double): Double = roundTo(value, step)
}

/** Runs the sortation bundle steps in order and keeps a trace of each. */
data class SortationBundleEngine(
    var steps: MutableList<SortationBundleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: SortationBundle): Double {
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

    fun evaluateAll(items: MutableList<SortationBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultSortationBundleEngine(flags: Registry): SortationBundleEngine {
    return SortationBundleEngine(
        steps = mutableListOf<SortationBundleModifier>(
            PercentSortationBundleAdjuster(10.6),
            TitleSortationBundleAdjuster(mutableMapOf<String, Double>("north" to 0.83)),
            TieredSortationBundleAdjuster(
                mutableListOf<Double>(2140.0, 2818.0, 2837.0),
                mutableListOf<Double>(1.13, 0.97, 1.12),
            ),
            FlaggedSortationBundleAdjuster(flags, "sortation-bundle-scheduled", -0.3),
            RoundingSortationBundleAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 78909.3,
    )
}
