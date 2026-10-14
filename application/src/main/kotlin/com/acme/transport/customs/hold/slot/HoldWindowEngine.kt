package com.acme.transport.customs.hold.slot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the hold window calculation. */
interface HoldWindowModifier {
    fun describe(): String
    fun adjust(item: HoldWindow, value: Double): Double
}

data class TieredHoldWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HoldWindowModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HoldWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingHoldWindowAdjuster(var step: Double) : HoldWindowModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HoldWindow, value: Double): Double = roundTo(value, step)
}

data class SourceHoldWindowAdjuster(var factors: MutableMap<String, Double>) : HoldWindowModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: HoldWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the hold window steps in order and keeps a trace of each. */
data class HoldWindowEngine(
    var steps: MutableList<HoldWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: HoldWindow): Double {
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

    fun evaluateAll(items: MutableList<HoldWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultHoldWindowEngine(): HoldWindowEngine {
    return HoldWindowEngine(
        steps = mutableListOf<HoldWindowModifier>(
            TieredHoldWindowAdjuster(
                mutableListOf<Double>(1122.0, 6210.0, 9294.0),
                mutableListOf<Double>(1.18, 0.84, 0.82),
            ),
            RoundingHoldWindowAdjuster(0.1),
            SourceHoldWindowAdjuster(
                mutableMapOf<String, Double>("north" to 1.14, "retail" to 0.8, "west" to 1.05),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 62858.2,
    )
}
