package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the cross dock preference calculation. */
interface CrossDockPreferenceModifier {
    fun describe(): String
    fun adjust(item: CrossDockPreference, value: Double): Double
}

data class MinimumCrossDockPreferenceAdjuster(var minimum: Double) : CrossDockPreferenceModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockPreference, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingCrossDockPreferenceAdjuster(var step: Double) : CrossDockPreferenceModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CrossDockPreference, value: Double): Double = roundTo(value, step)
}

data class CapCrossDockPreferenceAdjuster(var cap: Double) : CrossDockPreferenceModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CrossDockPreference, value: Double): Double = minOf(value, cap)
}

/** Runs the cross dock preference steps in order and keeps a trace of each. */
data class CrossDockPreferenceEngine(
    var steps: MutableList<CrossDockPreferenceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CrossDockPreference): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CrossDockPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCrossDockPreferenceEngine(): CrossDockPreferenceEngine {
    return CrossDockPreferenceEngine(
        steps = mutableListOf<CrossDockPreferenceModifier>(
            MinimumCrossDockPreferenceAdjuster(2.8),
            RoundingCrossDockPreferenceAdjuster(0.05),
            CapCrossDockPreferenceAdjuster(10547.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 41364.2,
    )
}
