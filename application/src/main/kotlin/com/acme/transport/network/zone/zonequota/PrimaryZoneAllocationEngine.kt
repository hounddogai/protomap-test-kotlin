package com.acme.transport.network.zone.zonequota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the primary zone allocation calculation. */
interface PrimaryZoneAllocationStep {
    fun describe(): String
    fun adjust(item: PrimaryZoneAllocation, value: Double): Double
}

data class FlaggedPrimaryZoneAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PrimaryZoneAllocationStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PrimaryZoneAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapPrimaryZoneAllocationAdjuster(var cap: Double) : PrimaryZoneAllocationStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PrimaryZoneAllocation, value: Double): Double = minOf(value, cap)
}

data class RoundingPrimaryZoneAllocationAdjuster(var step: Double) : PrimaryZoneAllocationStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PrimaryZoneAllocation, value: Double): Double = roundTo(value, step)
}

data class MinimumPrimaryZoneAllocationAdjuster(var minimum: Double) : PrimaryZoneAllocationStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PrimaryZoneAllocation, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PrimaryZoneAllocationGraceDaysOffsetAdjuster(var weight: Double) : PrimaryZoneAllocationStep {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: PrimaryZoneAllocation, value: Double): Double {
        return value + item.graceDays.toDouble() * weight
    }
}

data class TieredPrimaryZoneAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PrimaryZoneAllocationStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PrimaryZoneAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the primary zone allocation steps in order and keeps a trace of each. */
data class PrimaryZoneAllocationEngine(
    var steps: MutableList<PrimaryZoneAllocationStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PrimaryZoneAllocation): Double {
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

    fun evaluateAll(items: MutableList<PrimaryZoneAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPrimaryZoneAllocationEngine(flags: Registry): PrimaryZoneAllocationEngine {
    return PrimaryZoneAllocationEngine(
        steps = mutableListOf<PrimaryZoneAllocationStep>(
            FlaggedPrimaryZoneAllocationAdjuster(flags, "primary-zone-allocation-default", 8.9),
            CapPrimaryZoneAllocationAdjuster(1525.0),
            RoundingPrimaryZoneAllocationAdjuster(1.0),
            MinimumPrimaryZoneAllocationAdjuster(38.2),
            PrimaryZoneAllocationGraceDaysOffsetAdjuster(0.97),
            TieredPrimaryZoneAllocationAdjuster(
                mutableListOf<Double>(3585.0, 9660.0),
                mutableListOf<Double>(0.96, 1.0),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.9,
        ceiling = 99565.3,
    )
}
