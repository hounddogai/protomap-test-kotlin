package com.acme.transport.customs.inspection.budget

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the primary inspection audit calculation. */
interface PrimaryInspectionAuditStep {
    fun describe(): String
    fun adjust(item: PrimaryInspectionAudit, value: Double): Double
}

data class PrimaryInspectionAuditVersionOffsetAdjuster(var weight: Double) : PrimaryInspectionAuditStep {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: PrimaryInspectionAudit, value: Double): Double {
        return value + item.version.toDouble() * weight
    }
}

data class PercentPrimaryInspectionAuditAdjuster(var percent: Double) : PrimaryInspectionAuditStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PrimaryInspectionAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPrimaryInspectionAuditAdjuster(var cap: Double) : PrimaryInspectionAuditStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryInspectionAudit, value: Double): Double = minOf(value, cap)
}

data class UnitPrimaryInspectionAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : PrimaryInspectionAuditStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: PrimaryInspectionAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredPrimaryInspectionAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryInspectionAuditStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryInspectionAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary inspection audit steps in order and keeps a trace of each. */
data class PrimaryInspectionAuditEngine(
    var steps: MutableList<PrimaryInspectionAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PrimaryInspectionAudit): Double {
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

    fun evaluateAll(items: MutableList<PrimaryInspectionAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPrimaryInspectionAuditEngine(): PrimaryInspectionAuditEngine {
    return PrimaryInspectionAuditEngine(
        steps = mutableListOf<PrimaryInspectionAuditStep>(
            PrimaryInspectionAuditVersionOffsetAdjuster(1.52),
            PercentPrimaryInspectionAuditAdjuster(-11.9),
            CapPrimaryInspectionAuditAdjuster(8884.0),
            UnitPrimaryInspectionAuditAdjuster(
                mutableMapOf<String, Double>("east" to 0.76, "online" to 0.92, "central" to 1.03),
            ),
            TieredPrimaryInspectionAuditAdjuster(
                mutableListOf<Double>(1292.0, 1858.0, 6205.0),
                mutableListOf<Double>(1.04, 1.15, 1.14),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 49428.3,
    )
}
