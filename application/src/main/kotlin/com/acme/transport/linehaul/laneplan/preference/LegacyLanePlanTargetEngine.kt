package com.acme.transport.linehaul.laneplan.preference

import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy lane plan target calculation. */
interface LegacyLanePlanTargetStep {
    fun describe(): String
    fun adjust(item: LegacyLanePlanTarget, value: Double): Double
}

data class LegacyLanePlanTargetWeightOffsetAdjuster(var weight: Double) : LegacyLanePlanTargetStep {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: LegacyLanePlanTarget, value: Double): Double = value + item.weight * weight
}

data class CapLegacyLanePlanTargetAdjuster(var cap: Double) : LegacyLanePlanTargetStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyLanePlanTarget, value: Double): Double = minOf(value, cap)
}

data class RoundingLegacyLanePlanTargetAdjuster(var step: Double) : LegacyLanePlanTargetStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyLanePlanTarget, value: Double): Double = roundTo(value, step)
}

data class PercentLegacyLanePlanTargetAdjuster(var percent: Double) : LegacyLanePlanTargetStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyLanePlanTarget, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy lane plan target steps in order and keeps a trace of each. */
data class LegacyLanePlanTargetEngine(
    var steps: MutableList<LegacyLanePlanTargetStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: LegacyLanePlanTarget): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyLanePlanTargetStep = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyLanePlanTarget>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultLegacyLanePlanTargetEngine(): LegacyLanePlanTargetEngine {
    return LegacyLanePlanTargetEngine(
        steps = mutableListOf<LegacyLanePlanTargetStep>(
            LegacyLanePlanTargetWeightOffsetAdjuster(1.73),
            CapLegacyLanePlanTargetAdjuster(4700.0),
            RoundingLegacyLanePlanTargetAdjuster(5.0),
            PercentLegacyLanePlanTargetAdjuster(-17.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 54841.7,
    )
}
