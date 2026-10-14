package com.acme.transport.emissions.fuelburn.burnrequest

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling fuel burn request calculation. */
interface RollingFuelBurnRequestModifier {
    fun describe(): String
    fun adjust(item: RollingFuelBurnRequest, value: Double): Double
}

data class RoundingRollingFuelBurnRequestAdjuster(var step: Double) : RollingFuelBurnRequestModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingFuelBurnRequest, value: Double): Double = roundTo(value, step)
}

data class PercentRollingFuelBurnRequestAdjuster(var percent: Double) : RollingFuelBurnRequestModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingFuelBurnRequest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRollingFuelBurnRequestAdjuster(var cap: Double) : RollingFuelBurnRequestModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingFuelBurnRequest, value: Double): Double = minOf(value, cap)
}

/** Runs the rolling fuel burn request steps in order and keeps a trace of each. */
data class RollingFuelBurnRequestEngine(
    var steps: MutableList<RollingFuelBurnRequestModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RollingFuelBurnRequest): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingFuelBurnRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRollingFuelBurnRequestEngine(): RollingFuelBurnRequestEngine {
    return RollingFuelBurnRequestEngine(
        steps = mutableListOf<RollingFuelBurnRequestModifier>(
            RoundingRollingFuelBurnRequestAdjuster(5.0),
            PercentRollingFuelBurnRequestAdjuster(24.5),
            CapRollingFuelBurnRequestAdjuster(7194.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 88015.4,
    )
}
