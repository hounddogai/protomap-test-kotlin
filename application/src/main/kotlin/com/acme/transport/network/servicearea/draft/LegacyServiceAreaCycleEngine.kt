package com.acme.transport.network.servicearea.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy service area cycle calculation. */
interface LegacyServiceAreaCycleModifier {
    fun describe(): String
    fun adjust(item: LegacyServiceAreaCycle, value: Double): Double
}

data class MinimumLegacyServiceAreaCycleAdjuster(var minimum: Double) : LegacyServiceAreaCycleModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyServiceAreaCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLegacyServiceAreaCycleAdjuster(var percent: Double) : LegacyServiceAreaCycleModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyServiceAreaCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLegacyServiceAreaCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyServiceAreaCycleModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyServiceAreaCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredLegacyServiceAreaCycleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyServiceAreaCycleModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyServiceAreaCycle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingLegacyServiceAreaCycleAdjuster(var step: Double) : LegacyServiceAreaCycleModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyServiceAreaCycle, value: Double): Double = roundTo(value, step)
}

data class RegionLegacyServiceAreaCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyServiceAreaCycleModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: LegacyServiceAreaCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the legacy service area cycle steps in order and keeps a trace of each. */
data class LegacyServiceAreaCycleEngine(
    var steps: MutableList<LegacyServiceAreaCycleModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyServiceAreaCycle): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyServiceAreaCycleModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyServiceAreaCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyServiceAreaCycleEngine(flags: Registry): LegacyServiceAreaCycleEngine {
    return LegacyServiceAreaCycleEngine(
        steps = mutableListOf<LegacyServiceAreaCycleModifier>(
            MinimumLegacyServiceAreaCycleAdjuster(11.9),
            PercentLegacyServiceAreaCycleAdjuster(-14.6),
            FlaggedLegacyServiceAreaCycleAdjuster(flags, "legacy-service-area-cycle-global", 6.1),
            TieredLegacyServiceAreaCycleAdjuster(
                mutableListOf<Double>(5504.0, 6856.0),
                mutableListOf<Double>(1.14, 1.12),
            ),
            RoundingLegacyServiceAreaCycleAdjuster(5.0),
            RegionLegacyServiceAreaCycleAdjuster(
                mutableMapOf<String, Double>("central" to 0.92, "retail" to 0.99),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 89553.8,
    )
}
