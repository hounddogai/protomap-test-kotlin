package com.acme.transport.fleetops.inspection.adjustment

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the inspection audit calculation. */
interface InspectionAuditStep {
    fun describe(): String
    fun adjust(item: InspectionAudit, value: Double): Double
}

data class RoundingInspectionAuditAdjuster(var step: Double) : InspectionAuditStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionAudit, value: Double): Double = roundTo(value, step)
}

data class InspectionAuditScoreOffsetAdjuster(var weight: Double) : InspectionAuditStep {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: InspectionAudit, value: Double): Double = value + item.score * weight
}

data class TieredInspectionAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionAuditStep {
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

/** Runs the inspection audit steps in order and keeps a trace of each. */
data class InspectionAuditEngine(
    var steps: MutableList<InspectionAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: InspectionAudit): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultInspectionAuditEngine(): InspectionAuditEngine {
    return InspectionAuditEngine(
        steps = mutableListOf<InspectionAuditStep>(
            RoundingInspectionAuditAdjuster(0.1),
            InspectionAuditScoreOffsetAdjuster(1.83),
            TieredInspectionAuditAdjuster(
                mutableListOf<Double>(2020.0, 3077.0, 9211.0),
                mutableListOf<Double>(0.97, 1.07, 0.91),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 30023.7,
    )
}
