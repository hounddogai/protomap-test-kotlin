package com.acme.transport.customs.tariff.schedule

import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted tariff audit calculation. */
interface WeightedTariffAuditAdjuster {
    fun describe(): String
    fun adjust(item: WeightedTariffAudit, value: Double): Double
}

data class RoundingWeightedTariffAuditAdjuster(var step: Double) : WeightedTariffAuditAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedTariffAudit, value: Double): Double = roundTo(value, step)
}

data class PercentWeightedTariffAuditAdjuster(var percent: Double) : WeightedTariffAuditAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: WeightedTariffAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumWeightedTariffAuditAdjuster(var minimum: Double) : WeightedTariffAuditAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: WeightedTariffAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the weighted tariff audit steps in order and keeps a trace of each. */
data class WeightedTariffAuditEngine(
    var steps: MutableList<WeightedTariffAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: WeightedTariffAudit): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedTariffAuditAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedTariffAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultWeightedTariffAuditEngine(): WeightedTariffAuditEngine {
    return WeightedTariffAuditEngine(
        steps = mutableListOf<WeightedTariffAuditAdjuster>(
            RoundingWeightedTariffAuditAdjuster(0.05),
            PercentWeightedTariffAuditAdjuster(8.3),
            MinimumWeightedTariffAuditAdjuster(48.2),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 1114.3,
    )
}
