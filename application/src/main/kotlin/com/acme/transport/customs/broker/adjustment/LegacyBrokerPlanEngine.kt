package com.acme.transport.customs.broker.adjustment

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy broker plan calculation. */
interface LegacyBrokerPlanAdjuster {
    fun describe(): String
    fun adjust(item: LegacyBrokerPlan, value: Double): Double
}

data class CapLegacyBrokerPlanAdjuster(var cap: Double) : LegacyBrokerPlanAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyBrokerPlan, value: Double): Double = minOf(value, cap)
}

data class PercentLegacyBrokerPlanAdjuster(var percent: Double) : LegacyBrokerPlanAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyBrokerPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumLegacyBrokerPlanAdjuster(var minimum: Double) : LegacyBrokerPlanAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyBrokerPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy broker plan steps in order and keeps a trace of each. */
data class LegacyBrokerPlanEngine(
    var steps: MutableList<LegacyBrokerPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyBrokerPlan): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyBrokerPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyBrokerPlanEngine(): LegacyBrokerPlanEngine {
    return LegacyBrokerPlanEngine(
        steps = mutableListOf<LegacyBrokerPlanAdjuster>(
            CapLegacyBrokerPlanAdjuster(17218.0),
            PercentLegacyBrokerPlanAdjuster(-14.7),
            MinimumLegacyBrokerPlanAdjuster(17.4),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 68538.3,
    )
}
