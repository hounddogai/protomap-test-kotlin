package com.acme.transport.fleetops.serviceplan.legacyplanrollup

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending service plan rollup calculation. */
interface PendingServicePlanRollupAdjuster {
    fun describe(): String
    fun adjust(item: PendingServicePlanRollup, value: Double): Double
}

data class CapPendingServicePlanRollupAdjuster(var cap: Double) : PendingServicePlanRollupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingServicePlanRollup, value: Double): Double = minOf(value, cap)
}

data class TieredPendingServicePlanRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingServicePlanRollupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingServicePlanRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentPendingServicePlanRollupAdjuster(var percent: Double) : PendingServicePlanRollupAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingServicePlanRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the pending service plan rollup steps in order and keeps a trace of each. */
data class PendingServicePlanRollupEngine(
    var steps: MutableList<PendingServicePlanRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingServicePlanRollup): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingServicePlanRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingServicePlanRollupEngine(): PendingServicePlanRollupEngine {
    return PendingServicePlanRollupEngine(
        steps = mutableListOf<PendingServicePlanRollupAdjuster>(
            CapPendingServicePlanRollupAdjuster(36456.0),
            TieredPendingServicePlanRollupAdjuster(
                mutableListOf<Double>(3015.0, 5530.0, 5997.0, 7152.0),
                mutableListOf<Double>(0.82, 1.19, 0.92, 0.98),
            ),
            PercentPendingServicePlanRollupAdjuster(5.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 68909.7,
    )
}
