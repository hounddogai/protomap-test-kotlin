package com.acme.transport.customs.broker.request

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the legacy broker baseline calculation. */
interface LegacyBrokerBaselineStep {
    fun describe(): String
    fun adjust(item: LegacyBrokerBaseline, value: Double): Double
}

data class RegionLegacyBrokerBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyBrokerBaselineStep {
    override fun describe(): String = "by region"

    override fun adjust(item: LegacyBrokerBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LegacyBrokerBaselineCostOffsetAdjuster(var weight: Double) : LegacyBrokerBaselineStep {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: LegacyBrokerBaseline, value: Double): Double = value + item.cost * weight
}

data class MinimumLegacyBrokerBaselineAdjuster(var minimum: Double) : LegacyBrokerBaselineStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyBrokerBaseline, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the legacy broker baseline steps in order and keeps a trace of each. */
data class LegacyBrokerBaselineEngine(
    var steps: MutableList<LegacyBrokerBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyBrokerBaseline): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyBrokerBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyBrokerBaselineEngine(): LegacyBrokerBaselineEngine {
    return LegacyBrokerBaselineEngine(
        steps = mutableListOf<LegacyBrokerBaselineStep>(
            RegionLegacyBrokerBaselineAdjuster(
                mutableMapOf<String, Double>("north" to 0.87, "online" to 0.86, "retail" to 1.06),
            ),
            LegacyBrokerBaselineCostOffsetAdjuster(0.5),
            MinimumLegacyBrokerBaselineAdjuster(27.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 28313.5,
    )
}
