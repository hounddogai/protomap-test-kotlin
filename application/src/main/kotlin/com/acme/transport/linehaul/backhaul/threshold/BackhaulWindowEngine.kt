package com.acme.transport.linehaul.backhaul.threshold

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the backhaul window calculation. */
interface BackhaulWindowModifier {
    fun describe(): String
    fun adjust(item: BackhaulWindow, value: Double): Double
}

data class TieredBackhaulWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BackhaulWindowModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BackhaulWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapBackhaulWindowAdjuster(var cap: Double) : BackhaulWindowModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BackhaulWindow, value: Double): Double = minOf(value, cap)
}

data class PercentBackhaulWindowAdjuster(var percent: Double) : BackhaulWindowModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BackhaulWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the backhaul window steps in order and keeps a trace of each. */
data class BackhaulWindowEngine(
    var steps: MutableList<BackhaulWindowModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BackhaulWindow): Double {
        var current = item.projectedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BackhaulWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBackhaulWindowEngine(): BackhaulWindowEngine {
    return BackhaulWindowEngine(
        steps = mutableListOf<BackhaulWindowModifier>(
            TieredBackhaulWindowAdjuster(
                mutableListOf<Double>(2681.0, 5828.0, 7106.0, 9763.0),
                mutableListOf<Double>(1.05, 1.08, 0.93, 1.01),
            ),
            CapBackhaulWindowAdjuster(32689.0),
            PercentBackhaulWindowAdjuster(-12.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 32005.6,
    )
}
