package com.acme.transport.lastmile.parcel.parcelreservation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted parcel override calculation. */
interface WeightedParcelOverrideStep {
    fun describe(): String
    fun adjust(item: WeightedParcelOverride, value: Double): Double
}

data class CapWeightedParcelOverrideAdjuster(var cap: Double) : WeightedParcelOverrideStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedParcelOverride, value: Double): Double = minOf(value, cap)
}

data class MinimumWeightedParcelOverrideAdjuster(var minimum: Double) : WeightedParcelOverrideStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedParcelOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredWeightedParcelOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedParcelOverrideStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedParcelOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedWeightedParcelOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedParcelOverrideStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedParcelOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class WeightedParcelOverrideBatchSizeOffsetAdjuster(var weight: Double) : WeightedParcelOverrideStep {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: WeightedParcelOverride, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class RoundingWeightedParcelOverrideAdjuster(var step: Double) : WeightedParcelOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedParcelOverride, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted parcel override steps in order and keeps a trace of each. */
data class WeightedParcelOverrideEngine(
    var steps: MutableList<WeightedParcelOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedParcelOverride): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedParcelOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedParcelOverrideEngine(flags: Registry): WeightedParcelOverrideEngine {
    return WeightedParcelOverrideEngine(
        steps = mutableListOf<WeightedParcelOverrideStep>(
            CapWeightedParcelOverrideAdjuster(1291.0),
            MinimumWeightedParcelOverrideAdjuster(20.7),
            TieredWeightedParcelOverrideAdjuster(
                mutableListOf<Double>(3067.0, 5064.0, 6102.0, 7662.0),
                mutableListOf<Double>(1.04, 1.18, 1.07, 0.87),
            ),
            FlaggedWeightedParcelOverrideAdjuster(flags, "weighted-parcel-override-pending", -3.2),
            WeightedParcelOverrideBatchSizeOffsetAdjuster(0.34),
            RoundingWeightedParcelOverrideAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 10614.7,
    )
}
