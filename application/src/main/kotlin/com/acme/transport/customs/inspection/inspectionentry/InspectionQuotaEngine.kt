package com.acme.transport.customs.inspection.inspectionentry

import com.acme.transport.kit.numbers.roundTo

/** One step of the inspection quota calculation. */
interface InspectionQuotaFactor {
    fun describe(): String
    fun adjust(item: InspectionQuota, value: Double): Double
}

data class CapInspectionQuotaAdjuster(var cap: Double) : InspectionQuotaFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionQuota, value: Double): Double = minOf(value, cap)
}

data class RoundingInspectionQuotaAdjuster(var step: Double) : InspectionQuotaFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionQuota, value: Double): Double = roundTo(value, step)
}

data class InspectionQuotaRevisionOffsetAdjuster(var weight: Double) : InspectionQuotaFactor {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: InspectionQuota, value: Double): Double = value + item.revision.toDouble() * weight
}

data class TieredInspectionQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionQuotaFactor {
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

data class DepotInspectionQuotaAdjuster(var factors: MutableMap<String, Double>) : InspectionQuotaFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: InspectionQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the inspection quota steps in order and keeps a trace of each. */
data class InspectionQuotaEngine(
    var steps: MutableList<InspectionQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: InspectionQuota): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionQuotaFactor = steps[index]
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
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultInspectionQuotaEngine(): InspectionQuotaEngine {
    return InspectionQuotaEngine(
        steps = mutableListOf<InspectionQuotaFactor>(
            CapInspectionQuotaAdjuster(33277.0),
            RoundingInspectionQuotaAdjuster(0.25),
            InspectionQuotaRevisionOffsetAdjuster(1.16),
            TieredInspectionQuotaAdjuster(
                mutableListOf<Double>(3035.0, 7498.0),
                mutableListOf<Double>(1.07, 1.02),
            ),
            DepotInspectionQuotaAdjuster(mutableMapOf<String, Double>("online" to 1.22, "retail" to 1.04)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 49378.1,
    )
}
