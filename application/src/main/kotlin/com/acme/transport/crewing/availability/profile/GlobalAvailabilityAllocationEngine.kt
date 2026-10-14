package com.acme.transport.crewing.availability.profile

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the global availability allocation calculation. */
interface GlobalAvailabilityAllocationModifier {
    fun describe(): String
    fun adjust(item: GlobalAvailabilityAllocation, value: Double): Double
}

data class NameGlobalAvailabilityAllocationAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalAvailabilityAllocationModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: GlobalAvailabilityAllocation, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapGlobalAvailabilityAllocationAdjuster(var cap: Double) : GlobalAvailabilityAllocationModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalAvailabilityAllocation, value: Double): Double = minOf(value, cap)
}

data class TieredGlobalAvailabilityAllocationAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalAvailabilityAllocationModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalAvailabilityAllocation, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentGlobalAvailabilityAllocationAdjuster(
    var percent: Double,
) : GlobalAvailabilityAllocationModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalAvailabilityAllocation, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedGlobalAvailabilityAllocationAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalAvailabilityAllocationModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalAvailabilityAllocation, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingGlobalAvailabilityAllocationAdjuster(
    var step: Double,
) : GlobalAvailabilityAllocationModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: GlobalAvailabilityAllocation, value: Double): Double = roundTo(value, step)
}

/** Runs the global availability allocation steps in order and keeps a trace of each. */
data class GlobalAvailabilityAllocationEngine(
    var steps: MutableList<GlobalAvailabilityAllocationModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalAvailabilityAllocation): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalAvailabilityAllocation>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalAvailabilityAllocationEngine(flags: Registry): GlobalAvailabilityAllocationEngine {
    return GlobalAvailabilityAllocationEngine(
        steps = mutableListOf<GlobalAvailabilityAllocationModifier>(
            NameGlobalAvailabilityAllocationAdjuster(
                mutableMapOf<String, Double>("west" to 1.0, "retail" to 1.39, "south" to 0.72),
            ),
            CapGlobalAvailabilityAllocationAdjuster(17801.0),
            TieredGlobalAvailabilityAllocationAdjuster(
                mutableListOf<Double>(1877.0, 2994.0, 3492.0, 9129.0),
                mutableListOf<Double>(1.0, 1.08, 1.11, 0.96),
            ),
            PercentGlobalAvailabilityAllocationAdjuster(-19.7),
            FlaggedGlobalAvailabilityAllocationAdjuster(flags, "global-availability-allocation-pending", 6.4),
            RoundingGlobalAvailabilityAllocationAdjuster(0.05),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 93485.6,
    )
}
