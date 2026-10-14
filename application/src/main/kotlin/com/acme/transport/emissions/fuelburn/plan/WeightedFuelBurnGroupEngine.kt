package com.acme.transport.emissions.fuelburn.plan

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted fuel burn group calculation. */
interface WeightedFuelBurnGroupFactor {
    fun describe(): String
    fun adjust(item: WeightedFuelBurnGroup, value: Double): Double
}

data class RoundingWeightedFuelBurnGroupAdjuster(var step: Double) : WeightedFuelBurnGroupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedFuelBurnGroup, value: Double): Double = roundTo(value, step)
}

data class WeightedFuelBurnGroupDistanceKmOffsetAdjuster(var weight: Double) : WeightedFuelBurnGroupFactor {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: WeightedFuelBurnGroup, value: Double): Double = value + item.distanceKm * weight
}

data class CapWeightedFuelBurnGroupAdjuster(var cap: Double) : WeightedFuelBurnGroupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedFuelBurnGroup, value: Double): Double = minOf(value, cap)
}

data class DepotWeightedFuelBurnGroupAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedFuelBurnGroupFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: WeightedFuelBurnGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredWeightedFuelBurnGroupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedFuelBurnGroupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedFuelBurnGroup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedWeightedFuelBurnGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedFuelBurnGroupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedFuelBurnGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted fuel burn group steps in order and keeps a trace of each. */
data class WeightedFuelBurnGroupEngine(
    var steps: MutableList<WeightedFuelBurnGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedFuelBurnGroup): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedFuelBurnGroupFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedFuelBurnGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedFuelBurnGroupEngine(flags: Registry): WeightedFuelBurnGroupEngine {
    return WeightedFuelBurnGroupEngine(
        steps = mutableListOf<WeightedFuelBurnGroupFactor>(
            RoundingWeightedFuelBurnGroupAdjuster(0.1),
            WeightedFuelBurnGroupDistanceKmOffsetAdjuster(0.09),
            CapWeightedFuelBurnGroupAdjuster(26727.0),
            DepotWeightedFuelBurnGroupAdjuster(mutableMapOf<String, Double>("north" to 1.22, "online" to 1.07)),
            TieredWeightedFuelBurnGroupAdjuster(
                mutableListOf<Double>(1428.0, 8518.0),
                mutableListOf<Double>(0.93, 0.9),
            ),
            FlaggedWeightedFuelBurnGroupAdjuster(flags, "weighted-fuel-burn-group-capped", 3.4),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 86926.5,
    )
}
