package com.acme.transport.emissions.idling.allocation

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the lenient idling allocation calculation. */
interface LenientIdlingAllocationModifier {
    fun describe(): String
    fun adjust(item: LenientIdlingAllocation, value: Double): Double
}

data class RegionLenientIdlingAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : LenientIdlingAllocationModifier {
    override fun describe(): String = "by region"

    override fun adjust(item: LenientIdlingAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RoundingLenientIdlingAllocationAdjuster(var step: Double) : LenientIdlingAllocationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LenientIdlingAllocation, value: Double): Double = roundTo(value, step)
}

data class MinimumLenientIdlingAllocationAdjuster(var minimum: Double) : LenientIdlingAllocationModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LenientIdlingAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LenientIdlingAllocationPriorityOffsetAdjuster(var weight: Double) : LenientIdlingAllocationModifier {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: LenientIdlingAllocation, value: Double): Double {
        return value + item.priority.toDouble() * weight
    }
}

data class FlaggedLenientIdlingAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LenientIdlingAllocationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LenientIdlingAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLenientIdlingAllocationAdjuster(var cap: Double) : LenientIdlingAllocationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LenientIdlingAllocation, value: Double): Double = minOf(value, cap)
}

/** Runs the lenient idling allocation steps in order and keeps a trace of each. */
data class LenientIdlingAllocationEngine(
    var steps: MutableList<LenientIdlingAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LenientIdlingAllocation): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<LenientIdlingAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLenientIdlingAllocationEngine(flags: Registry): LenientIdlingAllocationEngine {
    return LenientIdlingAllocationEngine(
        steps = mutableListOf<LenientIdlingAllocationModifier>(
            RegionLenientIdlingAllocationAdjuster(mutableMapOf<String, Double>("west" to 0.82, "east" to 1.3)),
            RoundingLenientIdlingAllocationAdjuster(0.25),
            MinimumLenientIdlingAllocationAdjuster(22.1),
            LenientIdlingAllocationPriorityOffsetAdjuster(1.21),
            FlaggedLenientIdlingAllocationAdjuster(flags, "lenient-idling-allocation-rolling", 9.4),
            CapLenientIdlingAllocationAdjuster(10027.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 58907.9,
    )
}
