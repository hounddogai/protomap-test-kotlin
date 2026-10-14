package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.numbers.roundTo

/** One step of the adaptive dock door baseline calculation. */
interface AdaptiveDockDoorBaselineStep {
    fun describe(): String
    fun adjust(item: AdaptiveDockDoorBaseline, value: Double): Double
}

data class RoundingAdaptiveDockDoorBaselineAdjuster(var step: Double) : AdaptiveDockDoorBaselineStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AdaptiveDockDoorBaseline, value: Double): Double = roundTo(value, step)
}

data class DepotAdaptiveDockDoorBaselineAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveDockDoorBaselineStep {
    override fun describe(): String = "by depot"

    override fun adjust(item: AdaptiveDockDoorBaseline, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentAdaptiveDockDoorBaselineAdjuster(var percent: Double) : AdaptiveDockDoorBaselineStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: AdaptiveDockDoorBaseline, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredAdaptiveDockDoorBaselineAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveDockDoorBaselineStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveDockDoorBaseline, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive dock door baseline steps in order and keeps a trace of each. */
data class AdaptiveDockDoorBaselineEngine(
    var steps: MutableList<AdaptiveDockDoorBaselineStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AdaptiveDockDoorBaseline): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveDockDoorBaselineStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveDockDoorBaseline>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAdaptiveDockDoorBaselineEngine(): AdaptiveDockDoorBaselineEngine {
    return AdaptiveDockDoorBaselineEngine(
        steps = mutableListOf<AdaptiveDockDoorBaselineStep>(
            RoundingAdaptiveDockDoorBaselineAdjuster(0.25),
            DepotAdaptiveDockDoorBaselineAdjuster(mutableMapOf<String, Double>("west" to 0.82)),
            PercentAdaptiveDockDoorBaselineAdjuster(11.2),
            TieredAdaptiveDockDoorBaselineAdjuster(
                mutableListOf<Double>(592.0, 7123.0),
                mutableListOf<Double>(1.15, 0.81),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.9,
        ceiling = 60983.5,
    )
}
