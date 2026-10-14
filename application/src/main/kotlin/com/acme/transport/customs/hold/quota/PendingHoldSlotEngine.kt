package com.acme.transport.customs.hold.quota

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending hold slot calculation. */
interface PendingHoldSlotAdjuster {
    fun describe(): String
    fun adjust(item: PendingHoldSlot, value: Double): Double
}

data class PercentPendingHoldSlotAdjuster(var percent: Double) : PendingHoldSlotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingHoldSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumPendingHoldSlotAdjuster(var minimum: Double) : PendingHoldSlotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingHoldSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PendingHoldSlotCeilingOffsetAdjuster(var weight: Double) : PendingHoldSlotAdjuster {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: PendingHoldSlot, value: Double): Double = value + item.ceiling * weight
}

data class RoundingPendingHoldSlotAdjuster(var step: Double) : PendingHoldSlotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingHoldSlot, value: Double): Double = roundTo(value, step)
}

data class UnitPendingHoldSlotAdjuster(var factors: MutableMap<String, Double>) : PendingHoldSlotAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: PendingHoldSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the pending hold slot steps in order and keeps a trace of each. */
data class PendingHoldSlotEngine(
    var steps: MutableList<PendingHoldSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingHoldSlot): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingHoldSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingHoldSlotEngine(): PendingHoldSlotEngine {
    return PendingHoldSlotEngine(
        steps = mutableListOf<PendingHoldSlotAdjuster>(
            PercentPendingHoldSlotAdjuster(19.4),
            MinimumPendingHoldSlotAdjuster(34.7),
            PendingHoldSlotCeilingOffsetAdjuster(0.87),
            RoundingPendingHoldSlotAdjuster(5.0),
            UnitPendingHoldSlotAdjuster(
                mutableMapOf<String, Double>("south" to 0.77, "north" to 1.24, "central" to 1.04),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.1,
        ceiling = 80907.4,
    )
}
