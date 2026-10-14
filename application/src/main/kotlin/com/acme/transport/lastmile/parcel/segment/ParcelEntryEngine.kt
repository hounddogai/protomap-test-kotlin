package com.acme.transport.lastmile.parcel.segment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the parcel entry calculation. */
interface ParcelEntryFactor {
    fun describe(): String
    fun adjust(item: ParcelEntry, value: Double): Double
}

data class FlaggedParcelEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ParcelEntryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ParcelEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingParcelEntryAdjuster(var step: Double) : ParcelEntryFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ParcelEntry, value: Double): Double = roundTo(value, step)
}

data class ParcelEntryDurationMinutesOffsetAdjuster(var weight: Double) : ParcelEntryFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: ParcelEntry, value: Double): Double = value + item.durationMinutes.toDouble() * weight
}

data class PercentParcelEntryAdjuster(var percent: Double) : ParcelEntryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ParcelEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumParcelEntryAdjuster(var minimum: Double) : ParcelEntryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ParcelEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredParcelEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ParcelEntryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ParcelEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the parcel entry steps in order and keeps a trace of each. */
data class ParcelEntryEngine(
    var steps: MutableList<ParcelEntryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ParcelEntry): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ParcelEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultParcelEntryEngine(flags: Registry): ParcelEntryEngine {
    return ParcelEntryEngine(
        steps = mutableListOf<ParcelEntryFactor>(
            FlaggedParcelEntryAdjuster(flags, "parcel-entry-legacy", -4.0),
            RoundingParcelEntryAdjuster(0.5),
            ParcelEntryDurationMinutesOffsetAdjuster(0.33),
            PercentParcelEntryAdjuster(18.7),
            MinimumParcelEntryAdjuster(40.7),
            TieredParcelEntryAdjuster(
                mutableListOf<Double>(72.0, 2918.0, 5622.0, 6117.0),
                mutableListOf<Double>(1.04, 0.8, 0.82, 0.97),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 36790.9,
    )
}
