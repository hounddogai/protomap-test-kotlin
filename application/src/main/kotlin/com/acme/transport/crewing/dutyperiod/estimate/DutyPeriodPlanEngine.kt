package com.acme.transport.crewing.dutyperiod.estimate

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the duty period plan calculation. */
interface DutyPeriodPlanAdjuster {
    fun describe(): String
    fun adjust(item: DutyPeriodPlan, value: Double): Double
}

data class RoundingDutyPeriodPlanAdjuster(var step: Double) : DutyPeriodPlanAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DutyPeriodPlan, value: Double): Double = roundTo(value, step)
}

data class PercentDutyPeriodPlanAdjuster(var percent: Double) : DutyPeriodPlanAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DutyPeriodPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredDutyPeriodPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DutyPeriodPlanAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DutyPeriodPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the duty period plan steps in order and keeps a trace of each. */
data class DutyPeriodPlanEngine(
    var steps: MutableList<DutyPeriodPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DutyPeriodPlan): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DutyPeriodPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDutyPeriodPlanEngine(): DutyPeriodPlanEngine {
    return DutyPeriodPlanEngine(
        steps = mutableListOf<DutyPeriodPlanAdjuster>(
            RoundingDutyPeriodPlanAdjuster(0.1),
            PercentDutyPeriodPlanAdjuster(-3.1),
            TieredDutyPeriodPlanAdjuster(
                mutableListOf<Double>(3277.0, 8502.0),
                mutableListOf<Double>(1.19, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 9708.6,
    )
}
