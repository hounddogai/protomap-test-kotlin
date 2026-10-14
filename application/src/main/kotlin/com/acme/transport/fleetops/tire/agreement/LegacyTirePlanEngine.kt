package com.acme.transport.fleetops.tire.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy tire plan calculation. */
interface LegacyTirePlanModifier {
    fun describe(): String
    fun adjust(item: LegacyTirePlan, value: Double): Double
}

data class UnitLegacyTirePlanAdjuster(var factors: MutableMap<String, Double>) : LegacyTirePlanModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: LegacyTirePlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentLegacyTirePlanAdjuster(var percent: Double) : LegacyTirePlanModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyTirePlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLegacyTirePlanAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyTirePlanModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyTirePlan, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingLegacyTirePlanAdjuster(var step: Double) : LegacyTirePlanModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyTirePlan, value: Double): Double = roundTo(value, step)
}

data class LegacyTirePlanMaximumOffsetAdjuster(var weight: Double) : LegacyTirePlanModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: LegacyTirePlan, value: Double): Double = value + item.maximum * weight
}

data class TieredLegacyTirePlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyTirePlanModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyTirePlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy tire plan steps in order and keeps a trace of each. */
data class LegacyTirePlanEngine(
    var steps: MutableList<LegacyTirePlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyTirePlan): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyTirePlanModifier = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyTirePlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyTirePlanEngine(flags: Registry): LegacyTirePlanEngine {
    return LegacyTirePlanEngine(
        steps = mutableListOf<LegacyTirePlanModifier>(
            UnitLegacyTirePlanAdjuster(mutableMapOf<String, Double>("retail" to 1.35)),
            PercentLegacyTirePlanAdjuster(13.4),
            FlaggedLegacyTirePlanAdjuster(flags, "legacy-tire-plan-scheduled", 1.1),
            RoundingLegacyTirePlanAdjuster(0.05),
            LegacyTirePlanMaximumOffsetAdjuster(0.87),
            TieredLegacyTirePlanAdjuster(
                mutableListOf<Double>(2482.0, 5664.0, 7146.0),
                mutableListOf<Double>(0.86, 0.86, 1.11),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 56318.7,
    )
}
