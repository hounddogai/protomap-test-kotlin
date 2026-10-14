package com.acme.transport.customs.hold.quota

import com.acme.transport.kit.numbers.roundTo

/** One step of the hold checkpoint calculation. */
interface HoldCheckpointFactor {
    fun describe(): String
    fun adjust(item: HoldCheckpoint, value: Double): Double
}

data class DepotHoldCheckpointAdjuster(var factors: MutableMap<String, Double>) : HoldCheckpointFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: HoldCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingHoldCheckpointAdjuster(var step: Double) : HoldCheckpointFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HoldCheckpoint, value: Double): Double = roundTo(value, step)
}

data class MinimumHoldCheckpointAdjuster(var minimum: Double) : HoldCheckpointFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: HoldCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the hold checkpoint steps in order and keeps a trace of each. */
data class HoldCheckpointEngine(
    var steps: MutableList<HoldCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: HoldCheckpoint): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: HoldCheckpointFactor = steps[index]
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

    fun evaluateAll(items: MutableList<HoldCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultHoldCheckpointEngine(): HoldCheckpointEngine {
    return HoldCheckpointEngine(
        steps = mutableListOf<HoldCheckpointFactor>(
            DepotHoldCheckpointAdjuster(mutableMapOf<String, Double>("west" to 0.79)),
            RoundingHoldCheckpointAdjuster(0.25),
            MinimumHoldCheckpointAdjuster(37.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 62794.0,
    )
}
