package com.acme.transport.crewing.availability.cycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the availability group calculation. */
interface AvailabilityGroupFactor {
    fun describe(): String
    fun adjust(item: AvailabilityGroup, value: Double): Double
}

data class RoundingAvailabilityGroupAdjuster(var step: Double) : AvailabilityGroupFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: AvailabilityGroup, value: Double): Double = roundTo(value, step)
}

data class NameAvailabilityGroupAdjuster(var factors: MutableMap<String, Double>) : AvailabilityGroupFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: AvailabilityGroup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAvailabilityGroupAdjuster(var minimum: Double) : AvailabilityGroupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilityGroup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAvailabilityGroupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AvailabilityGroupFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AvailabilityGroup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapAvailabilityGroupAdjuster(var cap: Double) : AvailabilityGroupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AvailabilityGroup, value: Double): Double = minOf(value, cap)
}

data class AvailabilityGroupBudgetOffsetAdjuster(var weight: Double) : AvailabilityGroupFactor {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: AvailabilityGroup, value: Double): Double = value + item.budget * weight
}

/** Runs the availability group steps in order and keeps a trace of each. */
data class AvailabilityGroupEngine(
    var steps: MutableList<AvailabilityGroupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AvailabilityGroup): Double {
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

    fun evaluateAll(items: MutableList<AvailabilityGroup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAvailabilityGroupEngine(flags: Registry): AvailabilityGroupEngine {
    return AvailabilityGroupEngine(
        steps = mutableListOf<AvailabilityGroupFactor>(
            RoundingAvailabilityGroupAdjuster(5.0),
            NameAvailabilityGroupAdjuster(
                mutableMapOf<String, Double>("central" to 1.32, "north" to 0.96, "online" to 1.39),
            ),
            MinimumAvailabilityGroupAdjuster(19.0),
            FlaggedAvailabilityGroupAdjuster(flags, "availability-group-legacy", 6.1),
            CapAvailabilityGroupAdjuster(12417.0),
            AvailabilityGroupBudgetOffsetAdjuster(0.09),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 97704.8,
    )
}
