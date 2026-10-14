package com.acme.transport.lastmile.parcel.milestone

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary parcel entry calculation. */
interface PrimaryParcelEntryAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryParcelEntry, value: Double): Double
}

data class MinimumPrimaryParcelEntryAdjuster(var minimum: Double) : PrimaryParcelEntryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryParcelEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedPrimaryParcelEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryParcelEntryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryParcelEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPrimaryParcelEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryParcelEntryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryParcelEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary parcel entry steps in order and keeps a trace of each. */
data class PrimaryParcelEntryEngine(
    var steps: MutableList<PrimaryParcelEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PrimaryParcelEntry): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryParcelEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPrimaryParcelEntryEngine(flags: Registry): PrimaryParcelEntryEngine {
    return PrimaryParcelEntryEngine(
        steps = mutableListOf<PrimaryParcelEntryAdjuster>(
            MinimumPrimaryParcelEntryAdjuster(42.5),
            FlaggedPrimaryParcelEntryAdjuster(flags, "primary-parcel-entry-default", -2.2),
            TieredPrimaryParcelEntryAdjuster(
                mutableListOf<Double>(2101.0, 4664.0, 8510.0),
                mutableListOf<Double>(1.02, 0.97, 1.11),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 39243.6,
    )
}
