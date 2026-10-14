package com.acme.transport.safety.nearmiss.plan

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy near miss plan calculation. */
interface LegacyNearMissPlanStep {
    fun describe(): String
    fun adjust(item: LegacyNearMissPlan, value: Double): Double
}

data class LegacyNearMissPlanQuantityOffsetAdjuster(var weight: Double) : LegacyNearMissPlanStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: LegacyNearMissPlan, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class RoundingLegacyNearMissPlanAdjuster(var step: Double) : LegacyNearMissPlanStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyNearMissPlan, value: Double): Double = roundTo(value, step)
}

data class PercentLegacyNearMissPlanAdjuster(var percent: Double) : LegacyNearMissPlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyNearMissPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLegacyNearMissPlanAdjuster(var cap: Double) : LegacyNearMissPlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyNearMissPlan, value: Double): Double = minOf(value, cap)
}

/** Runs the legacy near miss plan steps in order and keeps a trace of each. */
data class LegacyNearMissPlanEngine(
    var steps: MutableList<LegacyNearMissPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyNearMissPlan): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyNearMissPlanStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyNearMissPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyNearMissPlanEngine(): LegacyNearMissPlanEngine {
    return LegacyNearMissPlanEngine(
        steps = mutableListOf<LegacyNearMissPlanStep>(
            LegacyNearMissPlanQuantityOffsetAdjuster(1.5),
            RoundingLegacyNearMissPlanAdjuster(0.25),
            PercentLegacyNearMissPlanAdjuster(12.4),
            CapLegacyNearMissPlanAdjuster(46156.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 55095.4,
    )
}
