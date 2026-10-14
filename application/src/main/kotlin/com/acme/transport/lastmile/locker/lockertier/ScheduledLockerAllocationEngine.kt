package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the scheduled locker allocation calculation. */
interface ScheduledLockerAllocationStep {
    fun describe(): String
    fun adjust(item: ScheduledLockerAllocation, value: Double): Double
}

data class TieredScheduledLockerAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledLockerAllocationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledLockerAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentScheduledLockerAllocationAdjuster(var percent: Double) : ScheduledLockerAllocationStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledLockerAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingScheduledLockerAllocationAdjuster(var step: Double) : ScheduledLockerAllocationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledLockerAllocation, value: Double): Double = roundTo(value, step)
}

data class MinimumScheduledLockerAllocationAdjuster(var minimum: Double) : ScheduledLockerAllocationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledLockerAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapScheduledLockerAllocationAdjuster(var cap: Double) : ScheduledLockerAllocationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledLockerAllocation, value: Double): Double = minOf(value, cap)
}

data class UnitScheduledLockerAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledLockerAllocationStep {
    override fun describe(): String = "by unit"

    override fun adjust(item: ScheduledLockerAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled locker allocation steps in order and keeps a trace of each. */
data class ScheduledLockerAllocationEngine(
    var steps: MutableList<ScheduledLockerAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledLockerAllocation): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledLockerAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledLockerAllocationEngine(): ScheduledLockerAllocationEngine {
    return ScheduledLockerAllocationEngine(
        steps = mutableListOf<ScheduledLockerAllocationStep>(
            TieredScheduledLockerAllocationAdjuster(
                mutableListOf<Double>(3269.0, 5376.0, 9602.0),
                mutableListOf<Double>(0.86, 0.82, 0.86),
            ),
            PercentScheduledLockerAllocationAdjuster(6.7),
            RoundingScheduledLockerAllocationAdjuster(0.25),
            MinimumScheduledLockerAllocationAdjuster(49.3),
            CapScheduledLockerAllocationAdjuster(11485.0),
            UnitScheduledLockerAllocationAdjuster(
                mutableMapOf<String, Double>("retail" to 0.87, "west" to 0.92, "online" to 0.94),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.1,
        ceiling = 91220.5,
    )
}
