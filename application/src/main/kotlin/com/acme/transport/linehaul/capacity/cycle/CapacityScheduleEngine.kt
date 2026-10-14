package com.acme.transport.linehaul.capacity.cycle

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capacity schedule calculation. */
interface CapacityScheduleFactor {
    fun describe(): String
    fun adjust(item: CapacitySchedule, value: Double): Double
}

data class UnitCapacityScheduleAdjuster(var factors: MutableMap<String, Double>) : CapacityScheduleFactor {
    override fun describe(): String = "by unit"

    override fun adjust(item: CapacitySchedule, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentCapacityScheduleAdjuster(var percent: Double) : CapacityScheduleFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CapacitySchedule, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCapacityScheduleAdjuster(var step: Double) : CapacityScheduleFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CapacitySchedule, value: Double): Double = roundTo(value, step)
}

data class CapacityScheduleCeilingOffsetAdjuster(var weight: Double) : CapacityScheduleFactor {
    override fun describe(): String = "plus ceiling times $weight"

    override fun adjust(item: CapacitySchedule, value: Double): Double = value + item.ceiling * weight
}

/** Runs the capacity schedule steps in order and keeps a trace of each. */
data class CapacityScheduleEngine(
    var steps: MutableList<CapacityScheduleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CapacitySchedule): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CapacitySchedule>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCapacityScheduleEngine(): CapacityScheduleEngine {
    return CapacityScheduleEngine(
        steps = mutableListOf<CapacityScheduleFactor>(
            UnitCapacityScheduleAdjuster(
                mutableMapOf<String, Double>("south" to 0.74, "east" to 1.15, "west" to 1.05),
            ),
            PercentCapacityScheduleAdjuster(2.5),
            RoundingCapacityScheduleAdjuster(0.05),
            CapacityScheduleCeilingOffsetAdjuster(1.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 96051.4,
    )
}
