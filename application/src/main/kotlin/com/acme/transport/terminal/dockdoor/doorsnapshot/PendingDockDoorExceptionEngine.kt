package com.acme.transport.terminal.dockdoor.doorsnapshot

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending dock door exception calculation. */
interface PendingDockDoorExceptionAdjuster {
    fun describe(): String
    fun adjust(item: PendingDockDoorException, value: Double): Double
}

data class RoundingPendingDockDoorExceptionAdjuster(var step: Double) : PendingDockDoorExceptionAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingDockDoorException, value: Double): Double = roundTo(value, step)
}

data class TieredPendingDockDoorExceptionAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingDockDoorExceptionAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingDockDoorException, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PendingDockDoorExceptionWindowDaysOffsetAdjuster(
    var weight: Double,
) : PendingDockDoorExceptionAdjuster {
    override fun describe(): String = "plus window days times $weight"

    override fun adjust(item: PendingDockDoorException, value: Double): Double {
        return value + item.windowDays.toDouble() * weight
    }
}

data class MinimumPendingDockDoorExceptionAdjuster(var minimum: Double) : PendingDockDoorExceptionAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingDockDoorException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the pending dock door exception steps in order and keeps a trace of each. */
data class PendingDockDoorExceptionEngine(
    var steps: MutableList<PendingDockDoorExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingDockDoorException): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingDockDoorException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingDockDoorExceptionEngine(): PendingDockDoorExceptionEngine {
    return PendingDockDoorExceptionEngine(
        steps = mutableListOf<PendingDockDoorExceptionAdjuster>(
            RoundingPendingDockDoorExceptionAdjuster(1.0),
            TieredPendingDockDoorExceptionAdjuster(
                mutableListOf<Double>(3607.0, 5927.0, 8635.0),
                mutableListOf<Double>(1.18, 0.98, 1.04),
            ),
            PendingDockDoorExceptionWindowDaysOffsetAdjuster(1.59),
            MinimumPendingDockDoorExceptionAdjuster(23.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.2,
        ceiling = 17756.1,
    )
}
