package com.acme.transport.crewing.qualification.plan

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the qualification plan calculation. */
interface QualificationPlanFactor {
    fun describe(): String
    fun adjust(item: QualificationPlan, value: Double): Double
}

data class TieredQualificationPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : QualificationPlanFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: QualificationPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentQualificationPlanAdjuster(var percent: Double) : QualificationPlanFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: QualificationPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class QualificationPlanPriorityOffsetAdjuster(var weight: Double) : QualificationPlanFactor {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: QualificationPlan, value: Double): Double = value + item.priority.toDouble() * weight
}

data class CapQualificationPlanAdjuster(var cap: Double) : QualificationPlanFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationPlan, value: Double): Double = minOf(value, cap)
}

data class RoundingQualificationPlanAdjuster(var step: Double) : QualificationPlanFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationPlan, value: Double): Double = roundTo(value, step)
}

/** Runs the qualification plan steps in order and keeps a trace of each. */
data class QualificationPlanEngine(
    var steps: MutableList<QualificationPlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: QualificationPlan): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<QualificationPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultQualificationPlanEngine(): QualificationPlanEngine {
    return QualificationPlanEngine(
        steps = mutableListOf<QualificationPlanFactor>(
            TieredQualificationPlanAdjuster(
                mutableListOf<Double>(2617.0, 6718.0),
                mutableListOf<Double>(1.1, 1.11),
            ),
            PercentQualificationPlanAdjuster(9.0),
            QualificationPlanPriorityOffsetAdjuster(0.8),
            CapQualificationPlanAdjuster(18727.0),
            RoundingQualificationPlanAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 76039.7,
    )
}
