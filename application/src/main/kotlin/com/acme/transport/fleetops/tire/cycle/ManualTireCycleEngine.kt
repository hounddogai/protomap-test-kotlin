package com.acme.transport.fleetops.tire.cycle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the manual tire cycle calculation. */
interface ManualTireCycleStep {
    fun describe(): String
    fun adjust(item: ManualTireCycle, value: Double): Double
}

data class PercentManualTireCycleAdjuster(var percent: Double) : ManualTireCycleStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualTireCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapManualTireCycleAdjuster(var cap: Double) : ManualTireCycleStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualTireCycle, value: Double): Double = minOf(value, cap)
}

data class RoundingManualTireCycleAdjuster(var step: Double) : ManualTireCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ManualTireCycle, value: Double): Double = roundTo(value, step)
}

/** Runs the manual tire cycle steps in order and keeps a trace of each. */
data class ManualTireCycleEngine(
    var steps: MutableList<ManualTireCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualTireCycle): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualTireCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualTireCycleEngine(): ManualTireCycleEngine {
    return ManualTireCycleEngine(
        steps = mutableListOf<ManualTireCycleStep>(
            PercentManualTireCycleAdjuster(-19.9),
            CapManualTireCycleAdjuster(11666.0),
            RoundingManualTireCycleAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 2042.5,
    )
}
