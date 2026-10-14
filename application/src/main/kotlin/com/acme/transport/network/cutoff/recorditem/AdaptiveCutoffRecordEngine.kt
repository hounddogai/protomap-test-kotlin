package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive cutoff record calculation. */
interface AdaptiveCutoffRecordModifier {
    fun describe(): String
    fun adjust(item: AdaptiveCutoffRecord, value: Double): Double
}

data class RoundingAdaptiveCutoffRecordAdjuster(var step: Double) : AdaptiveCutoffRecordModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveCutoffRecord, value: Double): Double = roundTo(value, step)
}

data class CapAdaptiveCutoffRecordAdjuster(var cap: Double) : AdaptiveCutoffRecordModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveCutoffRecord, value: Double): Double = minOf(value, cap)
}

data class MinimumAdaptiveCutoffRecordAdjuster(var minimum: Double) : AdaptiveCutoffRecordModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveCutoffRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the adaptive cutoff record steps in order and keeps a trace of each. */
data class AdaptiveCutoffRecordEngine(
    var steps: MutableList<AdaptiveCutoffRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AdaptiveCutoffRecord): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveCutoffRecordModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveCutoffRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAdaptiveCutoffRecordEngine(): AdaptiveCutoffRecordEngine {
    return AdaptiveCutoffRecordEngine(
        steps = mutableListOf<AdaptiveCutoffRecordModifier>(
            RoundingAdaptiveCutoffRecordAdjuster(5.0),
            CapAdaptiveCutoffRecordAdjuster(38075.0),
            MinimumAdaptiveCutoffRecordAdjuster(34.1),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 70487.4,
    )
}
