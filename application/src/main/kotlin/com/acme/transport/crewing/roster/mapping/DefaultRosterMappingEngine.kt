package com.acme.transport.crewing.roster.mapping

import com.acme.transport.kit.numbers.roundTo

/** One step of the default roster mapping calculation. */
interface DefaultRosterMappingStep {
    fun describe(): String
    fun adjust(item: DefaultRosterMapping, value: Double): Double
}

data class DefaultRosterMappingOdometerOffsetAdjuster(var weight: Double) : DefaultRosterMappingStep {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: DefaultRosterMapping, value: Double): Double = value + item.odometer.toDouble() * weight
}

data class RoundingDefaultRosterMappingAdjuster(var step: Double) : DefaultRosterMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DefaultRosterMapping, value: Double): Double = roundTo(value, step)
}

data class LabelDefaultRosterMappingAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultRosterMappingStep {
    override fun describe(): String = "by label"

    override fun adjust(item: DefaultRosterMapping, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the default roster mapping steps in order and keeps a trace of each. */
data class DefaultRosterMappingEngine(
    var steps: MutableList<DefaultRosterMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: DefaultRosterMapping): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultRosterMappingStep = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultRosterMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultDefaultRosterMappingEngine(): DefaultRosterMappingEngine {
    return DefaultRosterMappingEngine(
        steps = mutableListOf<DefaultRosterMappingStep>(
            DefaultRosterMappingOdometerOffsetAdjuster(0.2),
            RoundingDefaultRosterMappingAdjuster(0.05),
            LabelDefaultRosterMappingAdjuster(
                mutableMapOf<String, Double>("central" to 0.75, "east" to 0.72, "south" to 1.36),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 79425.5,
    )
}
