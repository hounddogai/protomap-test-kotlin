package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the trailer rollup calculation. */
interface TrailerRollupModifier {
    fun describe(): String
    fun adjust(item: TrailerRollup, value: Double): Double
}

data class LaneTrailerRollupAdjuster(var factors: MutableMap<String, Double>) : TrailerRollupModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: TrailerRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTrailerRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TrailerRollupModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TrailerRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentTrailerRollupAdjuster(var percent: Double) : TrailerRollupModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TrailerRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedTrailerRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TrailerRollupModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TrailerRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TrailerRollupQuantityOffsetAdjuster(var weight: Double) : TrailerRollupModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: TrailerRollup, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class CapTrailerRollupAdjuster(var cap: Double) : TrailerRollupModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TrailerRollup, value: Double): Double = minOf(value, cap)
}

/** Runs the trailer rollup steps in order and keeps a trace of each. */
data class TrailerRollupEngine(
    var steps: MutableList<TrailerRollupModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: TrailerRollup): Double {
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

    fun evaluateAll(items: MutableList<TrailerRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultTrailerRollupEngine(flags: Registry): TrailerRollupEngine {
    return TrailerRollupEngine(
        steps = mutableListOf<TrailerRollupModifier>(
            LaneTrailerRollupAdjuster(
                mutableMapOf<String, Double>("central" to 0.71, "east" to 1.05, "retail" to 0.98),
            ),
            TieredTrailerRollupAdjuster(
                mutableListOf<Double>(2189.0, 2262.0, 5625.0),
                mutableListOf<Double>(0.8, 1.2, 0.88),
            ),
            PercentTrailerRollupAdjuster(-15.5),
            FlaggedTrailerRollupAdjuster(flags, "trailer-rollup-strict", -7.0),
            TrailerRollupQuantityOffsetAdjuster(0.59),
            CapTrailerRollupAdjuster(15848.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 56912.1,
    )
}
