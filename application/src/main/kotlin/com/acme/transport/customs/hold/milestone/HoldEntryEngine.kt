package com.acme.transport.customs.hold.milestone

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the hold entry calculation. */
interface HoldEntryStep {
    fun describe(): String
    fun adjust(item: HoldEntry, value: Double): Double
}

data class HoldEntryPriorityOffsetAdjuster(var weight: Double) : HoldEntryStep {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: HoldEntry, value: Double): Double = value + item.priority.toDouble() * weight
}

data class TieredHoldEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HoldEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HoldEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentHoldEntryAdjuster(var percent: Double) : HoldEntryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HoldEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingHoldEntryAdjuster(var step: Double) : HoldEntryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HoldEntry, value: Double): Double = roundTo(value, step)
}

/** Runs the hold entry steps in order and keeps a trace of each. */
data class HoldEntryEngine(
    var steps: MutableList<HoldEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: HoldEntry): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<HoldEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultHoldEntryEngine(): HoldEntryEngine {
    return HoldEntryEngine(
        steps = mutableListOf<HoldEntryStep>(
            HoldEntryPriorityOffsetAdjuster(1.97),
            TieredHoldEntryAdjuster(
                mutableListOf<Double>(3304.0, 7754.0, 9959.0),
                mutableListOf<Double>(0.85, 0.97, 1.19),
            ),
            PercentHoldEntryAdjuster(1.0),
            RoundingHoldEntryAdjuster(0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 34975.0,
    )
}
