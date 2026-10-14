package com.acme.transport.terminal.dockdoor.doorscore

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending dock door score calculation. */
interface PendingDockDoorScoreStep {
    fun describe(): String
    fun adjust(item: PendingDockDoorScore, value: Double): Double
}

data class RoundingPendingDockDoorScoreAdjuster(var step: Double) : PendingDockDoorScoreStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingDockDoorScore, value: Double): Double = roundTo(value, step)
}

data class MinimumPendingDockDoorScoreAdjuster(var minimum: Double) : PendingDockDoorScoreStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingDockDoorScore, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentPendingDockDoorScoreAdjuster(var percent: Double) : PendingDockDoorScoreStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingDockDoorScore, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapPendingDockDoorScoreAdjuster(var cap: Double) : PendingDockDoorScoreStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingDockDoorScore, value: Double): Double = minOf(value, cap)
}

data class ChannelPendingDockDoorScoreAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingDockDoorScoreStep {
    override fun describe(): String = "by channel"

    override fun adjust(item: PendingDockDoorScore, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending dock door score steps in order and keeps a trace of each. */
data class PendingDockDoorScoreEngine(
    var steps: MutableList<PendingDockDoorScoreStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingDockDoorScore): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingDockDoorScore>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingDockDoorScoreEngine(): PendingDockDoorScoreEngine {
    return PendingDockDoorScoreEngine(
        steps = mutableListOf<PendingDockDoorScoreStep>(
            RoundingPendingDockDoorScoreAdjuster(5.0),
            MinimumPendingDockDoorScoreAdjuster(43.0),
            PercentPendingDockDoorScoreAdjuster(-22.4),
            CapPendingDockDoorScoreAdjuster(33344.0),
            ChannelPendingDockDoorScoreAdjuster(
                mutableMapOf<String, Double>("north" to 0.75, "central" to 0.73, "online" to 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 88860.8,
    )
}
