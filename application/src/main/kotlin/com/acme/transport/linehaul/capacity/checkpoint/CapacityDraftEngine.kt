package com.acme.transport.linehaul.capacity.checkpoint

import com.acme.transport.kit.numbers.roundTo

/** One step of the capacity draft calculation. */
interface CapacityDraftAdjuster {
    fun describe(): String
    fun adjust(item: CapacityDraft, value: Double): Double
}

data class UnitCapacityDraftAdjuster(var factors: MutableMap<String, Double>) : CapacityDraftAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: CapacityDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCapacityDraftAdjuster(var minimum: Double) : CapacityDraftAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CapacityDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapCapacityDraftAdjuster(var cap: Double) : CapacityDraftAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityDraft, value: Double): Double = minOf(value, cap)
}

data class CapacityDraftAmountOffsetAdjuster(var weight: Double) : CapacityDraftAdjuster {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: CapacityDraft, value: Double): Double = value + item.amount * weight
}

data class RoundingCapacityDraftAdjuster(var step: Double) : CapacityDraftAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CapacityDraft, value: Double): Double = roundTo(value, step)
}

/** Runs the capacity draft steps in order and keeps a trace of each. */
data class CapacityDraftEngine(
    var steps: MutableList<CapacityDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CapacityDraft): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CapacityDraftAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<CapacityDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCapacityDraftEngine(): CapacityDraftEngine {
    return CapacityDraftEngine(
        steps = mutableListOf<CapacityDraftAdjuster>(
            UnitCapacityDraftAdjuster(mutableMapOf<String, Double>("east" to 0.74)),
            MinimumCapacityDraftAdjuster(37.5),
            CapCapacityDraftAdjuster(36425.0),
            CapacityDraftAmountOffsetAdjuster(1.05),
            RoundingCapacityDraftAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 3.0,
        ceiling = 27649.6,
    )
}
