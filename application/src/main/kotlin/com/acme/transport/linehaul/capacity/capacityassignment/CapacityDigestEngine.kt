package com.acme.transport.linehaul.capacity.capacityassignment

import com.acme.transport.kit.numbers.roundTo

/** One step of the capacity digest calculation. */
interface CapacityDigestFactor {
    fun describe(): String
    fun adjust(item: CapacityDigest, value: Double): Double
}

data class RoundingCapacityDigestAdjuster(var step: Double) : CapacityDigestFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CapacityDigest, value: Double): Double = roundTo(value, step)
}

data class PercentCapacityDigestAdjuster(var percent: Double) : CapacityDigestFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CapacityDigest, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapCapacityDigestAdjuster(var cap: Double) : CapacityDigestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CapacityDigest, value: Double): Double = minOf(value, cap)
}

data class CapacityDigestMinimumOffsetAdjuster(var weight: Double) : CapacityDigestFactor {
    override fun describe(): String = "plus minimum times $weight"

    override fun adjust(item: CapacityDigest, value: Double): Double = value + item.minimum * weight
}

/** Runs the capacity digest steps in order and keeps a trace of each. */
data class CapacityDigestEngine(
    var steps: MutableList<CapacityDigestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CapacityDigest): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CapacityDigestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CapacityDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCapacityDigestEngine(): CapacityDigestEngine {
    return CapacityDigestEngine(
        steps = mutableListOf<CapacityDigestFactor>(
            RoundingCapacityDigestAdjuster(1.0),
            PercentCapacityDigestAdjuster(-9.5),
            CapCapacityDigestAdjuster(14774.0),
            CapacityDigestMinimumOffsetAdjuster(0.57),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 9065.0,
    )
}
