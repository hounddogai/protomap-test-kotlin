package com.acme.transport.crewing.qualification.audit

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the qualification audit calculation. */
interface QualificationAuditAdjuster {
    fun describe(): String
    fun adjust(item: QualificationAudit, value: Double): Double
}

data class RoundingQualificationAuditAdjuster(var step: Double) : QualificationAuditAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationAudit, value: Double): Double = roundTo(value, step)
}

data class CapQualificationAuditAdjuster(var cap: Double) : QualificationAuditAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationAudit, value: Double): Double = minOf(value, cap)
}

data class TieredQualificationAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : QualificationAuditAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: QualificationAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentQualificationAuditAdjuster(var percent: Double) : QualificationAuditAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class QualificationAuditLimitOffsetAdjuster(var weight: Double) : QualificationAuditAdjuster {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: QualificationAudit, value: Double): Double = value + item.limit.toDouble() * weight
}

/** Runs the qualification audit steps in order and keeps a trace of each. */
data class QualificationAuditEngine(
    var steps: MutableList<QualificationAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: QualificationAudit): Double {
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

    fun evaluateAll(items: MutableList<QualificationAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultQualificationAuditEngine(): QualificationAuditEngine {
    return QualificationAuditEngine(
        steps = mutableListOf<QualificationAuditAdjuster>(
            RoundingQualificationAuditAdjuster(1.0),
            CapQualificationAuditAdjuster(25784.0),
            TieredQualificationAuditAdjuster(
                mutableListOf<Double>(3905.0, 3918.0, 9562.0),
                mutableListOf<Double>(1.13, 1.13, 1.16),
            ),
            PercentQualificationAuditAdjuster(-22.3),
            QualificationAuditLimitOffsetAdjuster(0.33),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 42596.3,
    )
}
