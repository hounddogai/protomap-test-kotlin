package com.acme.transport.fleetops.tire.tier

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled tire score calculation. */
interface ScheduledTireScoreAdjuster {
    fun describe(): String
    fun adjust(item: ScheduledTireScore, value: Double): Double
}

data class ScheduledTireScoreCeilingOffsetAdjuster(var weight: Double) : ScheduledTireScoreAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: ScheduledTireScore, value: Double): Double = value + item.ceiling * weight
}

data class TitleScheduledTireScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledTireScoreAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: ScheduledTireScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapScheduledTireScoreAdjuster(var cap: Double) : ScheduledTireScoreAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledTireScore, value: Double): Double = minOf(value, cap)
}

/** Runs the scheduled tire score steps in order and keeps a trace of each. */
data class ScheduledTireScoreEngine(
    var steps: MutableList<ScheduledTireScoreAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledTireScore): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledTireScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledTireScoreEngine(): ScheduledTireScoreEngine {
    return ScheduledTireScoreEngine(
        steps = mutableListOf<ScheduledTireScoreAdjuster>(
            ScheduledTireScoreCeilingOffsetAdjuster(0.27),
            TitleScheduledTireScoreAdjuster(mutableMapOf<String, Double>("online" to 0.95, "south" to 1.18)),
            CapScheduledTireScoreAdjuster(19495.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.0,
        ceiling = 1950.5,
    )
}
