package com.acme.transport.fleetops.utilization.audit

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the weighted utilization audit calculation. */
interface WeightedUtilizationAuditFactor {
    fun describe(): String
    fun adjust(item: WeightedUtilizationAudit, value: Double): Double
}

data class PercentWeightedUtilizationAuditAdjuster(var percent: Double) : WeightedUtilizationAuditFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedUtilizationAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapWeightedUtilizationAuditAdjuster(var cap: Double) : WeightedUtilizationAuditFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedUtilizationAudit, value: Double): Double = minOf(value, cap)
}

data class WeightedUtilizationAuditGraceDaysOffsetAdjuster(
    var weight: Double,
) : WeightedUtilizationAuditFactor {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: WeightedUtilizationAudit, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class RoundingWeightedUtilizationAuditAdjuster(var step: Double) : WeightedUtilizationAuditFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedUtilizationAudit, value: Double): Double = roundTo(value, step)
}

data class MinimumWeightedUtilizationAuditAdjuster(var minimum: Double) : WeightedUtilizationAuditFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedUtilizationAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the weighted utilization audit steps in order and keeps a trace of each. */
data class WeightedUtilizationAuditEngine(
    var steps: MutableList<WeightedUtilizationAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedUtilizationAudit): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<WeightedUtilizationAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedUtilizationAuditEngine(): WeightedUtilizationAuditEngine {
    return WeightedUtilizationAuditEngine(
        steps = mutableListOf<WeightedUtilizationAuditFactor>(
            PercentWeightedUtilizationAuditAdjuster(2.3),
            CapWeightedUtilizationAuditAdjuster(47872.0),
            WeightedUtilizationAuditGraceDaysOffsetAdjuster(0.21),
            RoundingWeightedUtilizationAuditAdjuster(0.05),
            MinimumWeightedUtilizationAuditAdjuster(40.9),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 99765.6,
    )
}
