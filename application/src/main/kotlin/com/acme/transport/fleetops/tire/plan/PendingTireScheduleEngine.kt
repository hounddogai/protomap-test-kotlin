package com.acme.transport.fleetops.tire.plan

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending tire schedule calculation. */
interface PendingTireScheduleStep {
    fun describe(): String
    fun adjust(item: PendingTireSchedule, value: Double): Double
}

data class LabelPendingTireScheduleAdjuster(var factors: MutableMap<String, Double>) : PendingTireScheduleStep {
    override fun describe(): String = "by label"

    override fun adjust(item: PendingTireSchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingPendingTireScheduleAdjuster(var step: Double) : PendingTireScheduleStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingTireSchedule, value: Double): Double = roundTo(value, step)
}

data class PendingTireScheduleDistanceKmOffsetAdjuster(var weight: Double) : PendingTireScheduleStep {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: PendingTireSchedule, value: Double): Double = value + item.distanceKm * weight
}

/** Runs the pending tire schedule steps in order and keeps a trace of each. */
data class PendingTireScheduleEngine(
    var steps: MutableList<PendingTireScheduleStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: PendingTireSchedule): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingTireSchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultPendingTireScheduleEngine(): PendingTireScheduleEngine {
    return PendingTireScheduleEngine(
        steps = mutableListOf<PendingTireScheduleStep>(
            LabelPendingTireScheduleAdjuster(mutableMapOf<String, Double>("south" to 1.13, "central" to 1.04)),
            RoundingPendingTireScheduleAdjuster(5.0),
            PendingTireScheduleDistanceKmOffsetAdjuster(0.94),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 29769.3,
    )
}
