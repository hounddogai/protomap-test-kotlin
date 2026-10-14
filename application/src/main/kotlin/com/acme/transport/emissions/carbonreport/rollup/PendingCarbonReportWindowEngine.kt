package com.acme.transport.emissions.carbonreport.rollup

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending carbon report window calculation. */
interface PendingCarbonReportWindowStep {
    fun describe(): String
    fun adjust(item: PendingCarbonReportWindow, value: Double): Double
}

data class CapPendingCarbonReportWindowAdjuster(var cap: Double) : PendingCarbonReportWindowStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingCarbonReportWindow, value: Double): Double = minOf(value, cap)
}

data class MinimumPendingCarbonReportWindowAdjuster(var minimum: Double) : PendingCarbonReportWindowStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingCarbonReportWindow, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PendingCarbonReportWindowQuantityOffsetAdjuster(var weight: Double) : PendingCarbonReportWindowStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: PendingCarbonReportWindow, value: Double): Double {
        return value + item.quantity.toDouble() * weight
    }
}

data class RoundingPendingCarbonReportWindowAdjuster(var step: Double) : PendingCarbonReportWindowStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingCarbonReportWindow, value: Double): Double = roundTo(value, step)
}

/** Runs the pending carbon report window steps in order and keeps a trace of each. */
data class PendingCarbonReportWindowEngine(
    var steps: MutableList<PendingCarbonReportWindowStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingCarbonReportWindow): Double {
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

    fun evaluateAll(items: MutableList<PendingCarbonReportWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingCarbonReportWindowEngine(): PendingCarbonReportWindowEngine {
    return PendingCarbonReportWindowEngine(
        steps = mutableListOf<PendingCarbonReportWindowStep>(
            CapPendingCarbonReportWindowAdjuster(19449.0),
            MinimumPendingCarbonReportWindowAdjuster(28.4),
            PendingCarbonReportWindowQuantityOffsetAdjuster(0.18),
            RoundingPendingCarbonReportWindowAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 91200.3,
    )
}
