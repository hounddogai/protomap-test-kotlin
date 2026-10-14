package com.acme.transport.network.hub.rollinghubentry

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled hub group calculation. */
interface ScheduledHubGroupStep {
    fun describe(): String
    fun adjust(item: ScheduledHubGroup, value: Double): Double
}

data class ScheduledHubGroupCapacityOffsetAdjuster(var weight: Double) : ScheduledHubGroupStep {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: ScheduledHubGroup, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class LaneScheduledHubGroupAdjuster(var factors: MutableMap<String, Double>) : ScheduledHubGroupStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: ScheduledHubGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingScheduledHubGroupAdjuster(var step: Double) : ScheduledHubGroupStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledHubGroup, value: Double): Double = roundTo(value, step)
}

/** Runs the scheduled hub group steps in order and keeps a trace of each. */
data class ScheduledHubGroupEngine(
    var steps: MutableList<ScheduledHubGroupStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledHubGroup): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledHubGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledHubGroupEngine(): ScheduledHubGroupEngine {
    return ScheduledHubGroupEngine(
        steps = mutableListOf<ScheduledHubGroupStep>(
            ScheduledHubGroupCapacityOffsetAdjuster(1.83),
            LaneScheduledHubGroupAdjuster(mutableMapOf<String, Double>("west" to 0.78, "south" to 1.12)),
            RoundingScheduledHubGroupAdjuster(0.25),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 88677.0,
    )
}
