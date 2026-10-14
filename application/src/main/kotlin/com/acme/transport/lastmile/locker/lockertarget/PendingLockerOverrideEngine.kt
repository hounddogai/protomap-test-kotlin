package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the pending locker override calculation. */
interface PendingLockerOverrideStep {
    fun describe(): String
    fun adjust(item: PendingLockerOverride, value: Double): Double
}

data class RegionPendingLockerOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingLockerOverrideStep {
    override fun describe(): String = "by region"

    override fun adjust(item: PendingLockerOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapPendingLockerOverrideAdjuster(var cap: Double) : PendingLockerOverrideStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingLockerOverride, value: Double): Double = minOf(value, cap)
}

data class TieredPendingLockerOverrideAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingLockerOverrideStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingLockerOverride, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPendingLockerOverrideAdjuster(var step: Double) : PendingLockerOverrideStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingLockerOverride, value: Double): Double = roundTo(value, step)
}

/** Runs the pending locker override steps in order and keeps a trace of each. */
data class PendingLockerOverrideEngine(
    var steps: MutableList<PendingLockerOverrideStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: PendingLockerOverride): Double {
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

    fun evaluateAll(items: MutableList<PendingLockerOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultPendingLockerOverrideEngine(): PendingLockerOverrideEngine {
    return PendingLockerOverrideEngine(
        steps = mutableListOf<PendingLockerOverrideStep>(
            RegionPendingLockerOverrideAdjuster(mutableMapOf<String, Double>("south" to 0.71, "east" to 1.1)),
            CapPendingLockerOverrideAdjuster(4454.0),
            TieredPendingLockerOverrideAdjuster(
                mutableListOf<Double>(2959.0, 6069.0, 8810.0, 9512.0),
                mutableListOf<Double>(0.87, 0.87, 0.88, 1.07),
            ),
            RoundingPendingLockerOverrideAdjuster(0.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.3,
        ceiling = 30726.4,
    )
}
