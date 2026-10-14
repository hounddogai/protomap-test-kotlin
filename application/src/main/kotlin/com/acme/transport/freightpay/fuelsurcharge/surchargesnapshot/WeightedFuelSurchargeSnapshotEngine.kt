package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted fuel surcharge snapshot calculation. */
interface WeightedFuelSurchargeSnapshotAdjuster {
    fun describe(): String
    fun adjust(item: WeightedFuelSurchargeSnapshot, value: Double): Double
}

data class CapWeightedFuelSurchargeSnapshotAdjuster(var cap: Double) : WeightedFuelSurchargeSnapshotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedFuelSurchargeSnapshot, value: Double): Double = minOf(value, cap)
}

data class DepotWeightedFuelSurchargeSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedFuelSurchargeSnapshotAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: WeightedFuelSurchargeSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumWeightedFuelSurchargeSnapshotAdjuster(
    var minimum: Double,
) : WeightedFuelSurchargeSnapshotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedFuelSurchargeSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingWeightedFuelSurchargeSnapshotAdjuster(
    var step: Double,
) : WeightedFuelSurchargeSnapshotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedFuelSurchargeSnapshot, value: Double): Double = roundTo(value, step)
}

data class TieredWeightedFuelSurchargeSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : WeightedFuelSurchargeSnapshotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: WeightedFuelSurchargeSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedWeightedFuelSurchargeSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedFuelSurchargeSnapshotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedFuelSurchargeSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted fuel surcharge snapshot steps in order and keeps a trace of each. */
data class WeightedFuelSurchargeSnapshotEngine(
    var steps: MutableList<WeightedFuelSurchargeSnapshotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: WeightedFuelSurchargeSnapshot): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedFuelSurchargeSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultWeightedFuelSurchargeSnapshotEngine(flags: Registry): WeightedFuelSurchargeSnapshotEngine {
    return WeightedFuelSurchargeSnapshotEngine(
        steps = mutableListOf<WeightedFuelSurchargeSnapshotAdjuster>(
            CapWeightedFuelSurchargeSnapshotAdjuster(47242.0),
            DepotWeightedFuelSurchargeSnapshotAdjuster(
                mutableMapOf<String, Double>("east" to 1.07, "online" to 0.92),
            ),
            MinimumWeightedFuelSurchargeSnapshotAdjuster(13.4),
            RoundingWeightedFuelSurchargeSnapshotAdjuster(0.5),
            TieredWeightedFuelSurchargeSnapshotAdjuster(
                mutableListOf<Double>(5189.0, 5900.0),
                mutableListOf<Double>(1.06, 0.9),
            ),
            FlaggedWeightedFuelSurchargeSnapshotAdjuster(
                flags,
                "weighted-fuel-surcharge-snapshot-regional",
                8.6,
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 14350.1,
    )
}
