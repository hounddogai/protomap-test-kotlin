package com.acme.transport.fleetops.serviceplan.legacyplanquota

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped service plan quota calculation. */
interface CappedServicePlanQuotaModifier {
    fun describe(): String
    fun adjust(item: CappedServicePlanQuota, value: Double): Double
}

data class CapCappedServicePlanQuotaAdjuster(var cap: Double) : CappedServicePlanQuotaModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedServicePlanQuota, value: Double): Double = minOf(value, cap)
}

data class MinimumCappedServicePlanQuotaAdjuster(var minimum: Double) : CappedServicePlanQuotaModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedServicePlanQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCappedServicePlanQuotaAdjuster(var step: Double) : CappedServicePlanQuotaModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedServicePlanQuota, value: Double): Double = roundTo(value, step)
}

/** Runs the capped service plan quota steps in order and keeps a trace of each. */
data class CappedServicePlanQuotaEngine(
    var steps: MutableList<CappedServicePlanQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedServicePlanQuota): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedServicePlanQuotaModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedServicePlanQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedServicePlanQuotaEngine(): CappedServicePlanQuotaEngine {
    return CappedServicePlanQuotaEngine(
        steps = mutableListOf<CappedServicePlanQuotaModifier>(
            CapCappedServicePlanQuotaAdjuster(6816.0),
            MinimumCappedServicePlanQuotaAdjuster(6.4),
            RoundingCappedServicePlanQuotaAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 78339.5,
    )
}
