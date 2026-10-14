package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the drop threshold calculation. */
interface DropThresholdStep {
    fun describe(): String
    fun adjust(item: DropThreshold, value: Double): Double
}

data class DropThresholdQuantityOffsetAdjuster(var weight: Double) : DropThresholdStep {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: DropThreshold, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class CapDropThresholdAdjuster(var cap: Double) : DropThresholdStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DropThreshold, value: Double): Double = minOf(value, cap)
}

data class PercentDropThresholdAdjuster(var percent: Double) : DropThresholdStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DropThreshold, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the drop threshold steps in order and keeps a trace of each. */
data class DropThresholdEngine(
    var steps: MutableList<DropThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DropThreshold): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DropThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDropThresholdEngine(): DropThresholdEngine {
    return DropThresholdEngine(
        steps = mutableListOf<DropThresholdStep>(
            DropThresholdQuantityOffsetAdjuster(0.94),
            CapDropThresholdAdjuster(37488.0),
            PercentDropThresholdAdjuster(3.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 25333.9,
    )
}
