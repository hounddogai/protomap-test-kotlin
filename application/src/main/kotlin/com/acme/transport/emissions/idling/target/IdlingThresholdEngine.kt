package com.acme.transport.emissions.idling.target

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the idling threshold calculation. */
interface IdlingThresholdFactor {
    fun describe(): String
    fun adjust(item: IdlingThreshold, value: Double): Double
}

data class PercentIdlingThresholdAdjuster(var percent: Double) : IdlingThresholdFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: IdlingThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapIdlingThresholdAdjuster(var cap: Double) : IdlingThresholdFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: IdlingThreshold, value: Double): Double = minOf(value, cap)
}

data class SegmentIdlingThresholdAdjuster(var factors: MutableMap<String, Double>) : IdlingThresholdFactor {
    override fun describe(): String = "by segment"

    override fun adjust(item: IdlingThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the idling threshold steps in order and keeps a trace of each. */
data class IdlingThresholdEngine(
    var steps: MutableList<IdlingThresholdFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: IdlingThreshold): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<IdlingThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultIdlingThresholdEngine(): IdlingThresholdEngine {
    return IdlingThresholdEngine(
        steps = mutableListOf<IdlingThresholdFactor>(
            PercentIdlingThresholdAdjuster(-12.2),
            CapIdlingThresholdAdjuster(40377.0),
            SegmentIdlingThresholdAdjuster(mutableMapOf<String, Double>("online" to 1.01, "central" to 1.07)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.3,
        ceiling = 31676.4,
    )
}
