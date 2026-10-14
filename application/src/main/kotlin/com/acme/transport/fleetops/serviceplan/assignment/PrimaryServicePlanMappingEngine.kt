package com.acme.transport.fleetops.serviceplan.assignment

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary service plan mapping calculation. */
interface PrimaryServicePlanMappingAdjuster {
    fun describe(): String
    fun adjust(item: PrimaryServicePlanMapping, value: Double): Double
}

data class MinimumPrimaryServicePlanMappingAdjuster(var minimum: Double) : PrimaryServicePlanMappingAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryServicePlanMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredPrimaryServicePlanMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryServicePlanMappingAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryServicePlanMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPrimaryServicePlanMappingAdjuster(var step: Double) : PrimaryServicePlanMappingAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryServicePlanMapping, value: Double): Double = roundTo(value, step)
}

data class CapPrimaryServicePlanMappingAdjuster(var cap: Double) : PrimaryServicePlanMappingAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryServicePlanMapping, value: Double): Double = minOf(value, cap)
}

data class FlaggedPrimaryServicePlanMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryServicePlanMappingAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryServicePlanMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the primary service plan mapping steps in order and keeps a trace of each. */
data class PrimaryServicePlanMappingEngine(
    var steps: MutableList<PrimaryServicePlanMappingAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PrimaryServicePlanMapping): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PrimaryServicePlanMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPrimaryServicePlanMappingEngine(flags: Registry): PrimaryServicePlanMappingEngine {
    return PrimaryServicePlanMappingEngine(
        steps = mutableListOf<PrimaryServicePlanMappingAdjuster>(
            MinimumPrimaryServicePlanMappingAdjuster(26.6),
            TieredPrimaryServicePlanMappingAdjuster(
                mutableListOf<Double>(1494.0, 3493.0, 7085.0),
                mutableListOf<Double>(0.97, 1.04, 0.87),
            ),
            RoundingPrimaryServicePlanMappingAdjuster(1.0),
            CapPrimaryServicePlanMappingAdjuster(38439.0),
            FlaggedPrimaryServicePlanMappingAdjuster(flags, "primary-service-plan-mapping-pending", 6.9),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 84833.6,
    )
}
