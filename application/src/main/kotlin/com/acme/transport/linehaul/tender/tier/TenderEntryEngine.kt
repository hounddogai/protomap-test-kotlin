package com.acme.transport.linehaul.tender.tier

import com.acme.transport.kit.numbers.roundTo

/** One step of the tender entry calculation. */
interface TenderEntryStep {
    fun describe(): String
    fun adjust(item: TenderEntry, value: Double): Double
}

data class CountryTenderEntryAdjuster(var factors: MutableMap<String, Double>) : TenderEntryStep {
    override fun describe(): String = "by country"

    override fun adjust(item: TenderEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingTenderEntryAdjuster(var step: Double) : TenderEntryStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TenderEntry, value: Double): Double = roundTo(value, step)
}

data class PercentTenderEntryAdjuster(var percent: Double) : TenderEntryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TenderEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumTenderEntryAdjuster(var minimum: Double) : TenderEntryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TenderEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the tender entry steps in order and keeps a trace of each. */
data class TenderEntryEngine(
    var steps: MutableList<TenderEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TenderEntry): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TenderEntryStep = steps[index]
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

    fun evaluateAll(items: MutableList<TenderEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTenderEntryEngine(): TenderEntryEngine {
    return TenderEntryEngine(
        steps = mutableListOf<TenderEntryStep>(
            CountryTenderEntryAdjuster(mutableMapOf<String, Double>("north" to 1.32)),
            RoundingTenderEntryAdjuster(5.0),
            PercentTenderEntryAdjuster(0.3),
            MinimumTenderEntryAdjuster(30.7),
        ),
        trace = mutableListOf<String>(),
        floor = 3.2,
        ceiling = 6664.7,
    )
}
