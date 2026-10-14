package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.numbers.roundTo

/** One step of the strict dock door score calculation. */
interface StrictDockDoorScoreStep {
    fun describe(): String
    fun adjust(item: StrictDockDoorScore, value: Double): Double
}

data class LabelStrictDockDoorScoreAdjuster(var factors: MutableMap<String, Double>) : StrictDockDoorScoreStep {
    override fun describe(): String = "by label"

    override fun adjust(item: StrictDockDoorScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumStrictDockDoorScoreAdjuster(var minimum: Double) : StrictDockDoorScoreStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictDockDoorScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingStrictDockDoorScoreAdjuster(var step: Double) : StrictDockDoorScoreStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictDockDoorScore, value: Double): Double = roundTo(value, step)
}

data class TieredStrictDockDoorScoreAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictDockDoorScoreStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictDockDoorScore, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the strict dock door score steps in order and keeps a trace of each. */
data class StrictDockDoorScoreEngine(
    var steps: MutableList<StrictDockDoorScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: StrictDockDoorScore): Double {
        var current = item.weightedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictDockDoorScoreStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictDockDoorScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultStrictDockDoorScoreEngine(): StrictDockDoorScoreEngine {
    return StrictDockDoorScoreEngine(
        steps = mutableListOf<StrictDockDoorScoreStep>(
            LabelStrictDockDoorScoreAdjuster(mutableMapOf<String, Double>("west" to 1.17, "south" to 1.19)),
            MinimumStrictDockDoorScoreAdjuster(23.3),
            RoundingStrictDockDoorScoreAdjuster(0.25),
            TieredStrictDockDoorScoreAdjuster(
                mutableListOf<Double>(1715.0, 6191.0, 7435.0, 9528.0),
                mutableListOf<Double>(0.92, 0.88, 1.14, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 91031.3,
    )
}
