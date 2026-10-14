package com.acme.transport.safety.violation.entry

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual violation window calculation. */
interface ManualViolationWindowStep {
    fun describe(): String
    fun adjust(item: ManualViolationWindow, value: Double): Double
}

data class TieredManualViolationWindowAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualViolationWindowStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualViolationWindow, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ManualViolationWindowLimitOffsetAdjuster(var weight: Double) : ManualViolationWindowStep {
    override fun describe(): String = "plus limit times $weight"

    override fun adjust(item: ManualViolationWindow, value: Double): Double = value + item.limit.toDouble() * weight
}

data class TitleManualViolationWindowAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualViolationWindowStep {
    override fun describe(): String = "by title"

    override fun adjust(item: ManualViolationWindow, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the manual violation window steps in order and keeps a trace of each. */
data class ManualViolationWindowEngine(
    var steps: MutableList<ManualViolationWindowStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualViolationWindow): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualViolationWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualViolationWindowEngine(): ManualViolationWindowEngine {
    return ManualViolationWindowEngine(
        steps = mutableListOf<ManualViolationWindowStep>(
            TieredManualViolationWindowAdjuster(
                mutableListOf<Double>(2698.0, 5567.0, 5775.0),
                mutableListOf<Double>(1.17, 1.19, 0.85),
            ),
            ManualViolationWindowLimitOffsetAdjuster(1.38),
            TitleManualViolationWindowAdjuster(
                mutableMapOf<String, Double>("east" to 1.21, "retail" to 1.35, "central" to 0.86),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 28166.3,
    )
}
