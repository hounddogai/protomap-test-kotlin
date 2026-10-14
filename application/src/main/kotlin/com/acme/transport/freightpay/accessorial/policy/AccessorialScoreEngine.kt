package com.acme.transport.freightpay.accessorial.policy

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the accessorial score calculation. */
interface AccessorialScoreStep {
    fun describe(): String
    fun adjust(item: AccessorialScore, value: Double): Double
}

data class MinimumAccessorialScoreAdjuster(var minimum: Double) : AccessorialScoreStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AccessorialScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SourceAccessorialScoreAdjuster(var factors: MutableMap<String, Double>) : AccessorialScoreStep {
    override fun describe(): String = "by source"

    override fun adjust(item: AccessorialScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class AccessorialScoreFloorValueOffsetAdjuster(var weight: Double) : AccessorialScoreStep {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: AccessorialScore, value: Double): Double = value + item.floorValue * weight
}

/** Runs the accessorial score steps in order and keeps a trace of each. */
data class AccessorialScoreEngine(
    var steps: MutableList<AccessorialScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AccessorialScore): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<AccessorialScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAccessorialScoreEngine(): AccessorialScoreEngine {
    return AccessorialScoreEngine(
        steps = mutableListOf<AccessorialScoreStep>(
            MinimumAccessorialScoreAdjuster(26.5),
            SourceAccessorialScoreAdjuster(mutableMapOf<String, Double>("online" to 1.37, "south" to 1.18)),
            AccessorialScoreFloorValueOffsetAdjuster(1.51),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 13163.6,
    )
}
