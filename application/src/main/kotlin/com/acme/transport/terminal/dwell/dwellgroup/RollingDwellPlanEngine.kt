package com.acme.transport.terminal.dwell.dwellgroup

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling dwell plan calculation. */
interface RollingDwellPlanAdjuster {
    fun describe(): String
    fun adjust(item: RollingDwellPlan, value: Double): Double
}

data class TieredRollingDwellPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingDwellPlanAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingDwellPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingRollingDwellPlanAdjuster(var step: Double) : RollingDwellPlanAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingDwellPlan, value: Double): Double = roundTo(value, step)
}

data class PercentRollingDwellPlanAdjuster(var percent: Double) : RollingDwellPlanAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingDwellPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RollingDwellPlanUpdatedAtOffsetAdjuster(var weight: Double) : RollingDwellPlanAdjuster {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: RollingDwellPlan, value: Double): Double = value + item.updatedAt.toDouble() * weight
}

/** Runs the rolling dwell plan steps in order and keeps a trace of each. */
data class RollingDwellPlanEngine(
    var steps: MutableList<RollingDwellPlanAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingDwellPlan): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingDwellPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingDwellPlanEngine(): RollingDwellPlanEngine {
    return RollingDwellPlanEngine(
        steps = mutableListOf<RollingDwellPlanAdjuster>(
            TieredRollingDwellPlanAdjuster(
                mutableListOf<Double>(3965.0, 9538.0, 9941.0),
                mutableListOf<Double>(0.93, 1.15, 1.01),
            ),
            RoundingRollingDwellPlanAdjuster(1.0),
            PercentRollingDwellPlanAdjuster(-5.1),
            RollingDwellPlanUpdatedAtOffsetAdjuster(1.49),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 61139.5,
    )
}
