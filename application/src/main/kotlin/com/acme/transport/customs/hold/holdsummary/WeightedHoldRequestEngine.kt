package com.acme.transport.customs.hold.holdsummary

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted hold request calculation. */
interface WeightedHoldRequestStep {
    fun describe(): String
    fun adjust(item: WeightedHoldRequest, value: Double): Double
}

data class RoundingWeightedHoldRequestAdjuster(var step: Double) : WeightedHoldRequestStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedHoldRequest, value: Double): Double = roundTo(value, step)
}

data class WeightedHoldRequestCapacityOffsetAdjuster(var weight: Double) : WeightedHoldRequestStep {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: WeightedHoldRequest, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class NameWeightedHoldRequestAdjuster(var factors: MutableMap<String, Double>) : WeightedHoldRequestStep {
    override fun describe(): String = "by name"

    override fun adjust(item: WeightedHoldRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapWeightedHoldRequestAdjuster(var cap: Double) : WeightedHoldRequestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: WeightedHoldRequest, value: Double): Double = minOf(value, cap)
}

data class FlaggedWeightedHoldRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : WeightedHoldRequestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: WeightedHoldRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the weighted hold request steps in order and keeps a trace of each. */
data class WeightedHoldRequestEngine(
    var steps: MutableList<WeightedHoldRequestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedHoldRequest): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedHoldRequestStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedHoldRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedHoldRequestEngine(flags: Registry): WeightedHoldRequestEngine {
    return WeightedHoldRequestEngine(
        steps = mutableListOf<WeightedHoldRequestStep>(
            RoundingWeightedHoldRequestAdjuster(1.0),
            WeightedHoldRequestCapacityOffsetAdjuster(1.0),
            NameWeightedHoldRequestAdjuster(mutableMapOf<String, Double>("retail" to 1.29, "east" to 1.28)),
            CapWeightedHoldRequestAdjuster(38494.0),
            FlaggedWeightedHoldRequestAdjuster(flags, "weighted-hold-request-weighted", 6.8),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 20749.4,
    )
}
