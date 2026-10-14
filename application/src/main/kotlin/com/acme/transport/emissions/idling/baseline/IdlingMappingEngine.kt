package com.acme.transport.emissions.idling.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the idling mapping calculation. */
interface IdlingMappingStep {
    fun describe(): String
    fun adjust(item: IdlingMapping, value: Double): Double
}

data class MinimumIdlingMappingAdjuster(var minimum: Double) : IdlingMappingStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: IdlingMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingIdlingMappingAdjuster(var step: Double) : IdlingMappingStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: IdlingMapping, value: Double): Double = roundTo(value, step)
}

data class FlaggedIdlingMappingAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : IdlingMappingStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: IdlingMapping, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class IdlingMappingQuantityOffsetAdjuster(var weight: Double) : IdlingMappingStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: IdlingMapping, value: Double): Double = value + item.quantity.toDouble() * weight
}

/** Runs the idling mapping steps in order and keeps a trace of each. */
data class IdlingMappingEngine(
    var steps: MutableList<IdlingMappingStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: IdlingMapping): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: IdlingMappingStep = steps[index]
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

    fun evaluateAll(items: MutableList<IdlingMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultIdlingMappingEngine(flags: Registry): IdlingMappingEngine {
    return IdlingMappingEngine(
        steps = mutableListOf<IdlingMappingStep>(
            MinimumIdlingMappingAdjuster(25.9),
            RoundingIdlingMappingAdjuster(0.5),
            FlaggedIdlingMappingAdjuster(flags, "idling-mapping-pending", 2.3),
            IdlingMappingQuantityOffsetAdjuster(1.08),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 87711.7,
    )
}
