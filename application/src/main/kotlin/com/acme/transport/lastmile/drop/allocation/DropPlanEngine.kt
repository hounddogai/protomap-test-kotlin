package com.acme.transport.lastmile.drop.allocation

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the drop plan calculation. */
interface DropPlanModifier {
    fun describe(): String
    fun adjust(item: DropPlan, value: Double): Double
}

data class DropPlanStartsAtOffsetAdjuster(var weight: Double) : DropPlanModifier {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: DropPlan, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class CapDropPlanAdjuster(var cap: Double) : DropPlanModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DropPlan, value: Double): Double = minOf(value, cap)
}

data class PercentDropPlanAdjuster(var percent: Double) : DropPlanModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DropPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the drop plan steps in order and keeps a trace of each. */
data class DropPlanEngine(
    var steps: MutableList<DropPlanModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DropPlan): Double {
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

    fun evaluateAll(items: MutableList<DropPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDropPlanEngine(): DropPlanEngine {
    return DropPlanEngine(
        steps = mutableListOf<DropPlanModifier>(
            DropPlanStartsAtOffsetAdjuster(1.29),
            CapDropPlanAdjuster(8021.0),
            PercentDropPlanAdjuster(16.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 13806.9,
    )
}
