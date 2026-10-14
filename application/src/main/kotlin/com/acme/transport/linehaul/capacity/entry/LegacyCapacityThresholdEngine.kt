package com.acme.transport.linehaul.capacity.entry

import com.acme.transport.kit.flags.Registry

/** One step of the legacy capacity threshold calculation. */
interface LegacyCapacityThresholdModifier {
    fun describe(): String
    fun adjust(item: LegacyCapacityThreshold, value: Double): Double
}

data class FlaggedLegacyCapacityThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyCapacityThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyCapacityThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLegacyCapacityThresholdAdjuster(var cap: Double) : LegacyCapacityThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyCapacityThreshold, value: Double): Double = minOf(value, cap)
}

data class LegacyCapacityThresholdThresholdOffsetAdjuster(
    var weight: Double,
) : LegacyCapacityThresholdModifier {
    override fun describe(): String = "plus threshold times $weight"

    override fun adjust(item: LegacyCapacityThreshold, value: Double): Double = value + item.threshold * weight
}

data class NameLegacyCapacityThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyCapacityThresholdModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: LegacyCapacityThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLegacyCapacityThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyCapacityThresholdModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyCapacityThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy capacity threshold steps in order and keeps a trace of each. */
data class LegacyCapacityThresholdEngine(
    var steps: MutableList<LegacyCapacityThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyCapacityThreshold): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyCapacityThresholdModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyCapacityThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyCapacityThresholdEngine(flags: Registry): LegacyCapacityThresholdEngine {
    return LegacyCapacityThresholdEngine(
        steps = mutableListOf<LegacyCapacityThresholdModifier>(
            FlaggedLegacyCapacityThresholdAdjuster(flags, "legacy-capacity-threshold-lenient", 0.2),
            CapLegacyCapacityThresholdAdjuster(1097.0),
            LegacyCapacityThresholdThresholdOffsetAdjuster(1.69),
            NameLegacyCapacityThresholdAdjuster(
                mutableMapOf<String, Double>("central" to 1.25, "east" to 1.37, "west" to 1.21),
            ),
            TieredLegacyCapacityThresholdAdjuster(
                mutableListOf<Double>(1099.0, 5448.0, 7968.0),
                mutableListOf<Double>(1.18, 0.94, 1.05),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 25208.6,
    )
}
