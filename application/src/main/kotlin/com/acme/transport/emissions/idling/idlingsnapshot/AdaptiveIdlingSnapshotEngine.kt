package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the adaptive idling snapshot calculation. */
interface AdaptiveIdlingSnapshotModifier {
    fun describe(): String
    fun adjust(item: AdaptiveIdlingSnapshot, value: Double): Double
}

data class PercentAdaptiveIdlingSnapshotAdjuster(var percent: Double) : AdaptiveIdlingSnapshotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveIdlingSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class AdaptiveIdlingSnapshotWeightOffsetAdjuster(var weight: Double) : AdaptiveIdlingSnapshotModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: AdaptiveIdlingSnapshot, value: Double): Double = value + item.weight * weight
}

data class TieredAdaptiveIdlingSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveIdlingSnapshotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveIdlingSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedAdaptiveIdlingSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AdaptiveIdlingSnapshotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AdaptiveIdlingSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumAdaptiveIdlingSnapshotAdjuster(var minimum: Double) : AdaptiveIdlingSnapshotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveIdlingSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingAdaptiveIdlingSnapshotAdjuster(var step: Double) : AdaptiveIdlingSnapshotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveIdlingSnapshot, value: Double): Double = roundTo(value, step)
}

/** Runs the adaptive idling snapshot steps in order and keeps a trace of each. */
data class AdaptiveIdlingSnapshotEngine(
    var steps: MutableList<AdaptiveIdlingSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveIdlingSnapshot): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AdaptiveIdlingSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveIdlingSnapshotEngine(flags: Registry): AdaptiveIdlingSnapshotEngine {
    return AdaptiveIdlingSnapshotEngine(
        steps = mutableListOf<AdaptiveIdlingSnapshotModifier>(
            PercentAdaptiveIdlingSnapshotAdjuster(-14.5),
            AdaptiveIdlingSnapshotWeightOffsetAdjuster(0.86),
            TieredAdaptiveIdlingSnapshotAdjuster(
                mutableListOf<Double>(484.0, 6081.0),
                mutableListOf<Double>(1.17, 0.98),
            ),
            FlaggedAdaptiveIdlingSnapshotAdjuster(flags, "adaptive-idling-snapshot-weighted", -5.3),
            MinimumAdaptiveIdlingSnapshotAdjuster(28.6),
            RoundingAdaptiveIdlingSnapshotAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 62088.0,
    )
}
