package com.acme.transport.linehaul.tender.plan

import com.acme.transport.kit.numbers.roundTo

/** One step of the tender group calculation. */
interface TenderGroupStep {
    fun describe(): String
    fun adjust(item: TenderGroup, value: Double): Double
}

data class MinimumTenderGroupAdjuster(var minimum: Double) : TenderGroupStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: TenderGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingTenderGroupAdjuster(var step: Double) : TenderGroupStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: TenderGroup, value: Double): Double = roundTo(value, step)
}

data class PercentTenderGroupAdjuster(var percent: Double) : TenderGroupStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: TenderGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the tender group steps in order and keeps a trace of each. */
data class TenderGroupEngine(
    var steps: MutableList<TenderGroupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: TenderGroup): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TenderGroupStep = steps[index]
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

    fun evaluateAll(items: MutableList<TenderGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultTenderGroupEngine(): TenderGroupEngine {
    return TenderGroupEngine(
        steps = mutableListOf<TenderGroupStep>(
            MinimumTenderGroupAdjuster(35.7),
            RoundingTenderGroupAdjuster(0.1),
            PercentTenderGroupAdjuster(5.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 50518.7,
    )
}
