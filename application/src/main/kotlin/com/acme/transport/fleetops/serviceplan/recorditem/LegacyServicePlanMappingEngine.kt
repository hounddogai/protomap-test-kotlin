package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy service plan mapping calculation. */
interface LegacyServicePlanMappingAdjuster {
    fun describe(): String
    fun adjust(item: LegacyServicePlanMapping, value: Double): Double
}

data class FlaggedLegacyServicePlanMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyServicePlanMappingAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyServicePlanMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingLegacyServicePlanMappingAdjuster(var step: Double) : LegacyServicePlanMappingAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyServicePlanMapping, value: Double): Double = roundTo(value, step)
}

data class TieredLegacyServicePlanMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyServicePlanMappingAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyServicePlanMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the legacy service plan mapping steps in order and keeps a trace of each. */
data class LegacyServicePlanMappingEngine(
    var steps: MutableList<LegacyServicePlanMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyServicePlanMapping): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyServicePlanMappingAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyServicePlanMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyServicePlanMappingEngine(flags: Registry): LegacyServicePlanMappingEngine {
    return LegacyServicePlanMappingEngine(
        steps = mutableListOf<LegacyServicePlanMappingAdjuster>(
            FlaggedLegacyServicePlanMappingAdjuster(flags, "legacy-service-plan-mapping-primary", 7.8),
            RoundingLegacyServicePlanMappingAdjuster(0.25),
            TieredLegacyServicePlanMappingAdjuster(
                mutableListOf<Double>(3364.0, 3818.0),
                mutableListOf<Double>(0.96, 0.96),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 73346.2,
    )
}
