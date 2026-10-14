package com.acme.transport.network.node.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the tiered node estimate calculation. */
interface TieredNodeEstimateModifier {
    fun describe(): String
    fun adjust(item: TieredNodeEstimate, value: Double): Double
}

data class FlaggedTieredNodeEstimateAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredNodeEstimateModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredNodeEstimate, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumTieredNodeEstimateAdjuster(var minimum: Double) : TieredNodeEstimateModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredNodeEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapTieredNodeEstimateAdjuster(var cap: Double) : TieredNodeEstimateModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredNodeEstimate, value: Double): Double = minOf(value, cap)
}

data class TieredNodeEstimateWeightOffsetAdjuster(var weight: Double) : TieredNodeEstimateModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: TieredNodeEstimate, value: Double): Double = value + item.weight * weight
}

data class TitleTieredNodeEstimateAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredNodeEstimateModifier {
    override fun describe(): String = "by title"

    override fun adjust(item: TieredNodeEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentTieredNodeEstimateAdjuster(var percent: Double) : TieredNodeEstimateModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TieredNodeEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tiered node estimate steps in order and keeps a trace of each. */
data class TieredNodeEstimateEngine(
    var steps: MutableList<TieredNodeEstimateModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredNodeEstimate): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<TieredNodeEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredNodeEstimateEngine(flags: Registry): TieredNodeEstimateEngine {
    return TieredNodeEstimateEngine(
        steps = mutableListOf<TieredNodeEstimateModifier>(
            FlaggedTieredNodeEstimateAdjuster(flags, "tiered-node-estimate-tiered", 6.1),
            MinimumTieredNodeEstimateAdjuster(36.9),
            CapTieredNodeEstimateAdjuster(26845.0),
            TieredNodeEstimateWeightOffsetAdjuster(1.49),
            TitleTieredNodeEstimateAdjuster(mutableMapOf<String, Double>("west" to 1.32, "central" to 1.15)),
            PercentTieredNodeEstimateAdjuster(-11.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.6,
        ceiling = 37470.5,
    )
}
