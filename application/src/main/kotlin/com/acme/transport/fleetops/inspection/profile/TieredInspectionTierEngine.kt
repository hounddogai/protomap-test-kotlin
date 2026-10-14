package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.kit.flags.Registry

/** One step of the tiered inspection tier calculation. */
interface TieredInspectionTierModifier {
    fun describe(): String
    fun adjust(item: TieredInspectionTier, value: Double): Double
}

data class FlaggedTieredInspectionTierAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : TieredInspectionTierModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: TieredInspectionTier, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CategoryTieredInspectionTierAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredInspectionTierModifier {
    override fun describe(): String = "by category"

    override fun adjust(item: TieredInspectionTier, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumTieredInspectionTierAdjuster(var minimum: Double) : TieredInspectionTierModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TieredInspectionTier, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredInspectionTierQuantityOffsetAdjuster(var weight: Double) : TieredInspectionTierModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: TieredInspectionTier, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class CapTieredInspectionTierAdjuster(var cap: Double) : TieredInspectionTierModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: TieredInspectionTier, value: Double): Double = minOf(value, cap)
}

data class TieredTieredInspectionTierAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredInspectionTierModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredInspectionTier, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered inspection tier steps in order and keeps a trace of each. */
data class TieredInspectionTierEngine(
    var steps: MutableList<TieredInspectionTierModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: TieredInspectionTier): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredInspectionTierModifier = steps[index]
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

    fun evaluateAll(items: MutableList<TieredInspectionTier>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultTieredInspectionTierEngine(flags: Registry): TieredInspectionTierEngine {
    return TieredInspectionTierEngine(
        steps = mutableListOf<TieredInspectionTierModifier>(
            FlaggedTieredInspectionTierAdjuster(flags, "tiered-inspection-tier-lenient", -9.6),
            CategoryTieredInspectionTierAdjuster(mutableMapOf<String, Double>("west" to 1.31)),
            MinimumTieredInspectionTierAdjuster(1.5),
            TieredInspectionTierQuantityOffsetAdjuster(0.46),
            CapTieredInspectionTierAdjuster(45845.0),
            TieredTieredInspectionTierAdjuster(
                mutableListOf<Double>(737.0, 2141.0, 6112.0, 6205.0),
                mutableListOf<Double>(0.96, 1.15, 0.83, 0.95),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 65436.1,
    )
}
