package com.acme.transport.fleetops.defect.defectrecord

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the defect audit calculation. */
interface DefectAuditFactor {
    fun describe(): String
    fun adjust(item: DefectAudit, value: Double): Double
}

data class DefectAuditLoadKgOffsetAdjuster(var weight: Double) : DefectAuditFactor {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: DefectAudit, value: Double): Double = value + item.loadKg * weight
}

data class RoundingDefectAuditAdjuster(var step: Double) : DefectAuditFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefectAudit, value: Double): Double = roundTo(value, step)
}

data class CapDefectAuditAdjuster(var cap: Double) : DefectAuditFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefectAudit, value: Double): Double = minOf(value, cap)
}

/** Runs the defect audit steps in order and keeps a trace of each. */
data class DefectAuditEngine(
    var steps: MutableList<DefectAuditFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefectAudit): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefectAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefectAuditEngine(): DefectAuditEngine {
    return DefectAuditEngine(
        steps = mutableListOf<DefectAuditFactor>(
            DefectAuditLoadKgOffsetAdjuster(0.21),
            RoundingDefectAuditAdjuster(0.05),
            CapDefectAuditAdjuster(22402.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 65708.4,
    )
}
