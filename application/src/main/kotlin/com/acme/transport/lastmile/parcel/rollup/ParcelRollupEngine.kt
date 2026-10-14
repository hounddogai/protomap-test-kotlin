package com.acme.transport.lastmile.parcel.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the parcel rollup calculation. */
interface ParcelRollupFactor {
    fun describe(): String
    fun adjust(item: ParcelRollup, value: Double): Double
}

data class FlaggedParcelRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ParcelRollupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ParcelRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryParcelRollupAdjuster(var factors: MutableMap<String, Double>) : ParcelRollupFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: ParcelRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapParcelRollupAdjuster(var cap: Double) : ParcelRollupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ParcelRollup, value: Double): Double = minOf(value, cap)
}

data class MinimumParcelRollupAdjuster(var minimum: Double) : ParcelRollupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ParcelRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ParcelRollupWindowDaysOffsetAdjuster(var weight: Double) : ParcelRollupFactor {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: ParcelRollup, value: Double): Double = value + item.windowDays.toDouble() * weight
}

/** Runs the parcel rollup steps in order and keeps a trace of each. */
data class ParcelRollupEngine(
    var steps: MutableList<ParcelRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ParcelRollup): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ParcelRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultParcelRollupEngine(flags: Registry): ParcelRollupEngine {
    return ParcelRollupEngine(
        steps = mutableListOf<ParcelRollupFactor>(
            FlaggedParcelRollupAdjuster(flags, "parcel-rollup-global", -5.4),
            CategoryParcelRollupAdjuster(mutableMapOf<String, Double>("retail" to 1.15)),
            CapParcelRollupAdjuster(47816.0),
            MinimumParcelRollupAdjuster(35.0),
            ParcelRollupWindowDaysOffsetAdjuster(1.02),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 47259.8,
    )
}
