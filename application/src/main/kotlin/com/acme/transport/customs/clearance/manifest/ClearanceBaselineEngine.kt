package com.acme.transport.customs.clearance.manifest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the clearance baseline calculation. */
interface ClearanceBaselineStep {
    fun describe(): String
    fun adjust(item: ClearanceBaseline, value: Double): Double
}

data class RoundingClearanceBaselineAdjuster(var step: Double) : ClearanceBaselineStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ClearanceBaseline, value: Double): Double = roundTo(value, step)
}

data class ClearanceBaselineRetriesOffsetAdjuster(var weight: Double) : ClearanceBaselineStep {
    override fun describe(): String = "plus retries times $weight"

    override fun adjust(item: ClearanceBaseline, value: Double): Double = value + item.retries.toDouble() * weight
}

data class PercentClearanceBaselineAdjuster(var percent: Double) : ClearanceBaselineStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ClearanceBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the clearance baseline steps in order and keeps a trace of each. */
data class ClearanceBaselineEngine(
    var steps: MutableList<ClearanceBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ClearanceBaseline): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ClearanceBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultClearanceBaselineEngine(): ClearanceBaselineEngine {
    return ClearanceBaselineEngine(
        steps = mutableListOf<ClearanceBaselineStep>(
            RoundingClearanceBaselineAdjuster(5.0),
            ClearanceBaselineRetriesOffsetAdjuster(1.06),
            PercentClearanceBaselineAdjuster(17.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 85345.8,
    )
}
