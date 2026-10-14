package com.acme.transport.network.hub.entry

import com.acme.transport.kit.numbers.roundTo

/** One step of the capped hub entry calculation. */
interface CappedHubEntryStep {
    fun describe(): String
    fun adjust(item: CappedHubEntry, value: Double): Double
}

data class RoundingCappedHubEntryAdjuster(var step: Double) : CappedHubEntryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedHubEntry, value: Double): Double = roundTo(value, step)
}

data class CappedHubEntryMarginOffsetAdjuster(var weight: Double) : CappedHubEntryStep {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: CappedHubEntry, value: Double): Double = value + item.margin * weight
}

data class TieredCappedHubEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedHubEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedHubEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped hub entry steps in order and keeps a trace of each. */
data class CappedHubEntryEngine(
    var steps: MutableList<CappedHubEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedHubEntry): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedHubEntryStep = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedHubEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedHubEntryEngine(): CappedHubEntryEngine {
    return CappedHubEntryEngine(
        steps = mutableListOf<CappedHubEntryStep>(
            RoundingCappedHubEntryAdjuster(5.0),
            CappedHubEntryMarginOffsetAdjuster(1.38),
            TieredCappedHubEntryAdjuster(
                mutableListOf<Double>(344.0, 1573.0),
                mutableListOf<Double>(0.85, 0.91),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 77888.5,
    )
}
