package com.acme.transport.customs.clearance.policy

import com.acme.transport.kit.numbers.roundTo

/** One step of the clearance audit calculation. */
interface ClearanceAuditFactor {
    fun describe(): String
    fun adjust(item: ClearanceAudit, value: Double): Double
}

data class RoundingClearanceAuditAdjuster(var step: Double) : ClearanceAuditFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ClearanceAudit, value: Double): Double = roundTo(value, step)
}

data class ClearanceAuditCostOffsetAdjuster(var weight: Double) : ClearanceAuditFactor {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: ClearanceAudit, value: Double): Double = value + item.cost * weight
}

data class TieredClearanceAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceAuditFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the clearance audit steps in order and keeps a trace of each. */
data class ClearanceAuditEngine(
    var steps: MutableList<ClearanceAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ClearanceAudit): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearanceAuditFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ClearanceAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultClearanceAuditEngine(): ClearanceAuditEngine {
    return ClearanceAuditEngine(
        steps = mutableListOf<ClearanceAuditFactor>(
            RoundingClearanceAuditAdjuster(0.1),
            ClearanceAuditCostOffsetAdjuster(0.18),
            TieredClearanceAuditAdjuster(
                mutableListOf<Double>(1583.0, 2264.0, 4470.0, 7004.0),
                mutableListOf<Double>(0.89, 0.86, 1.03, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.3,
        ceiling = 79104.1,
    )
}
