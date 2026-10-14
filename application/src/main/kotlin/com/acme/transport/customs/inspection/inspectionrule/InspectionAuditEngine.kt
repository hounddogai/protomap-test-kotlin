package com.acme.transport.customs.inspection.inspectionrule

import com.acme.transport.kit.numbers.roundTo

/** One step of the inspection audit calculation. */
interface InspectionAuditFactor {
    fun describe(): String
    fun adjust(item: InspectionAudit, value: Double): Double
}

data class RoundingInspectionAuditAdjuster(var step: Double) : InspectionAuditFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionAudit, value: Double): Double = roundTo(value, step)
}

data class PercentInspectionAuditAdjuster(var percent: Double) : InspectionAuditFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumInspectionAuditAdjuster(var minimum: Double) : InspectionAuditFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredInspectionAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionAuditFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DepotInspectionAuditAdjuster(var factors: MutableMap<String, Double>) : InspectionAuditFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: InspectionAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the inspection audit steps in order and keeps a trace of each. */
data class InspectionAuditEngine(
    var steps: MutableList<InspectionAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: InspectionAudit): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionAuditFactor = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultInspectionAuditEngine(): InspectionAuditEngine {
    return InspectionAuditEngine(
        steps = mutableListOf<InspectionAuditFactor>(
            RoundingInspectionAuditAdjuster(0.5),
            PercentInspectionAuditAdjuster(8.4),
            MinimumInspectionAuditAdjuster(40.7),
            TieredInspectionAuditAdjuster(
                mutableListOf<Double>(3564.0, 6986.0, 7956.0, 8770.0),
                mutableListOf<Double>(0.84, 0.97, 1.02, 0.98),
            ),
            DepotInspectionAuditAdjuster(
                mutableMapOf<String, Double>("west" to 1.12, "south" to 1.01, "central" to 0.76),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.4,
        ceiling = 10927.4,
    )
}
