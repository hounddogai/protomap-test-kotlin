package com.acme.transport.fleetops.serviceplan.planmetric

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending service plan metric calculation. */
interface PendingServicePlanMetricFactor {
    fun describe(): String
    fun adjust(item: PendingServicePlanMetric, value: Double): Double
}

data class PendingServicePlanMetricWeightOffsetAdjuster(var weight: Double) : PendingServicePlanMetricFactor {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: PendingServicePlanMetric, value: Double): Double = value + item.weight * weight
}

data class UnitPendingServicePlanMetricAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingServicePlanMetricFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: PendingServicePlanMetric, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedPendingServicePlanMetricAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingServicePlanMetricFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingServicePlanMetric, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the pending service plan metric steps in order and keeps a trace of each. */
data class PendingServicePlanMetricEngine(
    var steps: MutableList<PendingServicePlanMetricFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingServicePlanMetric): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingServicePlanMetric>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingServicePlanMetricEngine(flags: Registry): PendingServicePlanMetricEngine {
    return PendingServicePlanMetricEngine(
        steps = mutableListOf<PendingServicePlanMetricFactor>(
            PendingServicePlanMetricWeightOffsetAdjuster(1.6),
            UnitPendingServicePlanMetricAdjuster(
                mutableMapOf<String, Double>("retail" to 0.85, "north" to 1.36),
            ),
            FlaggedPendingServicePlanMetricAdjuster(flags, "pending-service-plan-metric-regional", -2.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 29634.4,
    )
}
