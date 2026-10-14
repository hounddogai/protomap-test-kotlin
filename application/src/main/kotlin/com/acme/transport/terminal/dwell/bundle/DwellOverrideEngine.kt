package com.acme.transport.terminal.dwell.bundle

import com.acme.transport.kit.numbers.roundTo

/** One step of the dwell override calculation. */
interface DwellOverrideFactor {
    fun describe(): String
    fun adjust(item: DwellOverride, value: Double): Double
}

data class PercentDwellOverrideAdjuster(var percent: Double) : DwellOverrideFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapDwellOverrideAdjuster(var cap: Double) : DwellOverrideFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DwellOverride, value: Double): Double = minOf(value, cap)
}

data class RoundingDwellOverrideAdjuster(var step: Double) : DwellOverrideFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DwellOverride, value: Double): Double = roundTo(value, step)
}

/** Runs the dwell override steps in order and keeps a trace of each. */
data class DwellOverrideEngine(
    var steps: MutableList<DwellOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DwellOverride): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DwellOverrideFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DwellOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDwellOverrideEngine(): DwellOverrideEngine {
    return DwellOverrideEngine(
        steps = mutableListOf<DwellOverrideFactor>(
            PercentDwellOverrideAdjuster(18.8),
            CapDwellOverrideAdjuster(19741.0),
            RoundingDwellOverrideAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 79035.5,
    )
}
