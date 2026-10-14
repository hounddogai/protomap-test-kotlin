package com.acme.transport.fleetops.serviceplan.planledger

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy service plan baseline calculation. */
interface LegacyServicePlanBaselineModifier {
    fun describe(): String
    fun adjust(item: LegacyServicePlanBaseline, value: Double): Double
}

data class PercentLegacyServicePlanBaselineAdjuster(var percent: Double) : LegacyServicePlanBaselineModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyServicePlanBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLegacyServicePlanBaselineAdjuster(var cap: Double) : LegacyServicePlanBaselineModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyServicePlanBaseline, value: Double): Double = minOf(value, cap)
}

data class TieredLegacyServicePlanBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyServicePlanBaselineModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyServicePlanBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LegacyServicePlanBaselineQuantityOffsetAdjuster(
    var weight: Double,
) : LegacyServicePlanBaselineModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: LegacyServicePlanBaseline, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class FlaggedLegacyServicePlanBaselineAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyServicePlanBaselineModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyServicePlanBaseline, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the legacy service plan baseline steps in order and keeps a trace of each. */
data class LegacyServicePlanBaselineEngine(
    var steps: MutableList<LegacyServicePlanBaselineModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyServicePlanBaseline): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyServicePlanBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyServicePlanBaselineEngine(flags: Registry): LegacyServicePlanBaselineEngine {
    return LegacyServicePlanBaselineEngine(
        steps = mutableListOf<LegacyServicePlanBaselineModifier>(
            PercentLegacyServicePlanBaselineAdjuster(2.5),
            CapLegacyServicePlanBaselineAdjuster(45833.0),
            TieredLegacyServicePlanBaselineAdjuster(
                mutableListOf<Double>(6414.0, 8854.0),
                mutableListOf<Double>(0.9, 1.13),
            ),
            LegacyServicePlanBaselineQuantityOffsetAdjuster(0.45),
            FlaggedLegacyServicePlanBaselineAdjuster(flags, "legacy-service-plan-baseline-legacy", -7.2),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 99742.8,
    )
}
