package com.acme.transport.linehaul.tender.group

import com.acme.transport.kit.numbers.roundTo

/** One step of the lenient tender group calculation. */
interface LenientTenderGroupStep {
    fun describe(): String
    fun adjust(item: LenientTenderGroup, value: Double): Double
}

data class RoundingLenientTenderGroupAdjuster(var step: Double) : LenientTenderGroupStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientTenderGroup, value: Double): Double = roundTo(value, step)
}

data class PercentLenientTenderGroupAdjuster(var percent: Double) : LenientTenderGroupStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LenientTenderGroup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CountryLenientTenderGroupAdjuster(var factors: MutableMap<String, Double>) : LenientTenderGroupStep {
    override fun describe(): String = "by country"

    override fun adjust(item: LenientTenderGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the lenient tender group steps in order and keeps a trace of each. */
data class LenientTenderGroupEngine(
    var steps: MutableList<LenientTenderGroupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LenientTenderGroup): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LenientTenderGroupStep = steps[index]
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

    fun evaluateAll(items: MutableList<LenientTenderGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLenientTenderGroupEngine(): LenientTenderGroupEngine {
    return LenientTenderGroupEngine(
        steps = mutableListOf<LenientTenderGroupStep>(
            RoundingLenientTenderGroupAdjuster(0.1),
            PercentLenientTenderGroupAdjuster(-6.9),
            CountryLenientTenderGroupAdjuster(
                mutableMapOf<String, Double>("central" to 0.79, "north" to 0.79, "online" to 1.23),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.0,
        ceiling = 13132.9,
    )
}
