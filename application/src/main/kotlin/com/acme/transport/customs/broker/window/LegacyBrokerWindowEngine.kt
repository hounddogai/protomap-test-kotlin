package com.acme.transport.customs.broker.window

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy broker window calculation. */
interface LegacyBrokerWindowAdjuster {
    fun describe(): String
    fun adjust(item: LegacyBrokerWindow, value: Double): Double
}

data class PercentLegacyBrokerWindowAdjuster(var percent: Double) : LegacyBrokerWindowAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyBrokerWindow, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapLegacyBrokerWindowAdjuster(var cap: Double) : LegacyBrokerWindowAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyBrokerWindow, value: Double): Double = minOf(value, cap)
}

data class RoundingLegacyBrokerWindowAdjuster(var step: Double) : LegacyBrokerWindowAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyBrokerWindow, value: Double): Double = roundTo(value, step)
}

/** Runs the legacy broker window steps in order and keeps a trace of each. */
data class LegacyBrokerWindowEngine(
    var steps: MutableList<LegacyBrokerWindowAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyBrokerWindow): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LegacyBrokerWindow>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyBrokerWindowEngine(): LegacyBrokerWindowEngine {
    return LegacyBrokerWindowEngine(
        steps = mutableListOf<LegacyBrokerWindowAdjuster>(
            PercentLegacyBrokerWindowAdjuster(-16.9),
            CapLegacyBrokerWindowAdjuster(40133.0),
            RoundingLegacyBrokerWindowAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 19150.8,
    )
}
