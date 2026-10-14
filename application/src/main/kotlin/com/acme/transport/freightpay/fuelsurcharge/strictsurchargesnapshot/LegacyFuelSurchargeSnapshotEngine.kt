package com.acme.transport.freightpay.fuelsurcharge.strictsurchargesnapshot

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy fuel surcharge snapshot calculation. */
interface LegacyFuelSurchargeSnapshotStep {
    fun describe(): String
    fun adjust(item: LegacyFuelSurchargeSnapshot, value: Double): Double
}

data class LegacyFuelSurchargeSnapshotLoadKgOffsetAdjuster(
    var weight: Double,
) : LegacyFuelSurchargeSnapshotStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: LegacyFuelSurchargeSnapshot, value: Double): Double = value + item.loadKg * weight
}

data class PercentLegacyFuelSurchargeSnapshotAdjuster(var percent: Double) : LegacyFuelSurchargeSnapshotStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyFuelSurchargeSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLegacyFuelSurchargeSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyFuelSurchargeSnapshotStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyFuelSurchargeSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapLegacyFuelSurchargeSnapshotAdjuster(var cap: Double) : LegacyFuelSurchargeSnapshotStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyFuelSurchargeSnapshot, value: Double): Double = minOf(value, cap)
}

data class RoundingLegacyFuelSurchargeSnapshotAdjuster(var step: Double) : LegacyFuelSurchargeSnapshotStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyFuelSurchargeSnapshot, value: Double): Double = roundTo(value, step)
}

data class LaneLegacyFuelSurchargeSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyFuelSurchargeSnapshotStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: LegacyFuelSurchargeSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the legacy fuel surcharge snapshot steps in order and keeps a trace of each. */
data class LegacyFuelSurchargeSnapshotEngine(
    var steps: MutableList<LegacyFuelSurchargeSnapshotStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyFuelSurchargeSnapshot): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyFuelSurchargeSnapshotStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyFuelSurchargeSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyFuelSurchargeSnapshotEngine(): LegacyFuelSurchargeSnapshotEngine {
    return LegacyFuelSurchargeSnapshotEngine(
        steps = mutableListOf<LegacyFuelSurchargeSnapshotStep>(
            LegacyFuelSurchargeSnapshotLoadKgOffsetAdjuster(1.68),
            PercentLegacyFuelSurchargeSnapshotAdjuster(-2.5),
            TieredLegacyFuelSurchargeSnapshotAdjuster(
                mutableListOf<Double>(2688.0, 5499.0),
                mutableListOf<Double>(1.06, 0.81),
            ),
            CapLegacyFuelSurchargeSnapshotAdjuster(8835.0),
            RoundingLegacyFuelSurchargeSnapshotAdjuster(0.5),
            LaneLegacyFuelSurchargeSnapshotAdjuster(
                mutableMapOf<String, Double>("retail" to 0.73, "east" to 0.81),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 15837.5,
    )
}
