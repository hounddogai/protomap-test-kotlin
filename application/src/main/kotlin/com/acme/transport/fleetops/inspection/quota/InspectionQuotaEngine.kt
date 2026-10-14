package com.acme.transport.fleetops.inspection.quota

import com.acme.transport.kit.flags.Registry

/** One step of the inspection quota calculation. */
interface InspectionQuotaModifier {
    fun describe(): String
    fun adjust(item: InspectionQuota, value: Double): Double
}

data class InspectionQuotaWeightOffsetAdjuster(var weight: Double) : InspectionQuotaModifier {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: InspectionQuota, value: Double): Double = value + item.weight * weight
}

data class FlaggedInspectionQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionQuotaModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredInspectionQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionQuotaModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the inspection quota steps in order and keeps a trace of each. */
data class InspectionQuotaEngine(
    var steps: MutableList<InspectionQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: InspectionQuota): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionQuotaModifier = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultInspectionQuotaEngine(flags: Registry): InspectionQuotaEngine {
    return InspectionQuotaEngine(
        steps = mutableListOf<InspectionQuotaModifier>(
            InspectionQuotaWeightOffsetAdjuster(0.23),
            FlaggedInspectionQuotaAdjuster(flags, "inspection-quota-manual", 9.8),
            TieredInspectionQuotaAdjuster(
                mutableListOf<Double>(322.0, 2066.0, 9310.0, 9877.0),
                mutableListOf<Double>(1.14, 1.17, 1.12, 1.15),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 77496.5,
    )
}
