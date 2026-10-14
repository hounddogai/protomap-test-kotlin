package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.kit.numbers.roundTo

/** One step of the weighted locker cycle calculation. */
interface WeightedLockerCycleStep {
    fun describe(): String
    fun adjust(item: WeightedLockerCycle, value: Double): Double
}

data class WeightedLockerCycleDistanceKmOffsetAdjuster(var weight: Double) : WeightedLockerCycleStep {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: WeightedLockerCycle, value: Double): Double = value + item.distanceKm * weight
}

data class SegmentWeightedLockerCycleAdjuster(
    var factors: MutableMap<String, Double>,
) : WeightedLockerCycleStep {
    override fun describe(): String = "by segment"

    override fun adjust(item: WeightedLockerCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingWeightedLockerCycleAdjuster(var step: Double) : WeightedLockerCycleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: WeightedLockerCycle, value: Double): Double = roundTo(value, step)
}

/** Runs the weighted locker cycle steps in order and keeps a trace of each. */
data class WeightedLockerCycleEngine(
    var steps: MutableList<WeightedLockerCycleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: WeightedLockerCycle): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: WeightedLockerCycleStep = steps[index]
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

    fun evaluateAll(items: MutableList<WeightedLockerCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultWeightedLockerCycleEngine(): WeightedLockerCycleEngine {
    return WeightedLockerCycleEngine(
        steps = mutableListOf<WeightedLockerCycleStep>(
            WeightedLockerCycleDistanceKmOffsetAdjuster(1.88),
            SegmentWeightedLockerCycleAdjuster(mutableMapOf<String, Double>("online" to 0.75, "south" to 1.38)),
            RoundingWeightedLockerCycleAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 99019.4,
    )
}
