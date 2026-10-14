package com.acme.transport.customs.broker.schedule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the broker schedule calculation. */
interface BrokerScheduleAdjuster {
    fun describe(): String
    fun adjust(item: BrokerSchedule, value: Double): Double
}

data class CapBrokerScheduleAdjuster(var cap: Double) : BrokerScheduleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BrokerSchedule, value: Double): Double = minOf(value, cap)
}

data class RoundingBrokerScheduleAdjuster(var step: Double) : BrokerScheduleAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerSchedule, value: Double): Double = roundTo(value, step)
}

data class PercentBrokerScheduleAdjuster(var percent: Double) : BrokerScheduleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerSchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the broker schedule steps in order and keeps a trace of each. */
data class BrokerScheduleEngine(
    var steps: MutableList<BrokerScheduleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BrokerSchedule): Double {
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

    fun evaluateAll(items: MutableList<BrokerSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBrokerScheduleEngine(): BrokerScheduleEngine {
    return BrokerScheduleEngine(
        steps = mutableListOf<BrokerScheduleAdjuster>(
            CapBrokerScheduleAdjuster(10724.0),
            RoundingBrokerScheduleAdjuster(0.25),
            PercentBrokerScheduleAdjuster(12.8),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 98509.3,
    )
}
