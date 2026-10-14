package com.acme.transport.fleetops.serviceplan.agreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped service plan rollup calculation. */
interface CappedServicePlanRollupStep {
    fun describe(): String
    fun adjust(item: CappedServicePlanRollup, value: Double): Double
}

data class TieredCappedServicePlanRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedServicePlanRollupStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedServicePlanRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CappedServicePlanRollupFloorValueOffsetAdjuster(var weight: Double) : CappedServicePlanRollupStep {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: CappedServicePlanRollup, value: Double): Double = value + item.floorValue * weight
}

data class RoundingCappedServicePlanRollupAdjuster(var step: Double) : CappedServicePlanRollupStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedServicePlanRollup, value: Double): Double = roundTo(value, step)
}

data class FlaggedCappedServicePlanRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedServicePlanRollupStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedServicePlanRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the capped service plan rollup steps in order and keeps a trace of each. */
data class CappedServicePlanRollupEngine(
    var steps: MutableList<CappedServicePlanRollupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedServicePlanRollup): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedServicePlanRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedServicePlanRollupEngine(flags: Registry): CappedServicePlanRollupEngine {
    return CappedServicePlanRollupEngine(
        steps = mutableListOf<CappedServicePlanRollupStep>(
            TieredCappedServicePlanRollupAdjuster(
                mutableListOf<Double>(5564.0, 9334.0),
                mutableListOf<Double>(1.14, 0.87),
            ),
            CappedServicePlanRollupFloorValueOffsetAdjuster(0.67),
            RoundingCappedServicePlanRollupAdjuster(1.0),
            FlaggedCappedServicePlanRollupAdjuster(flags, "capped-service-plan-rollup-scheduled", -3.8),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 79647.4,
    )
}
