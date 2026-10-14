package com.acme.transport.safety.violation.manualviolationquota

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the fallback violation slot calculation. */
interface FallbackViolationSlotFactor {
    fun describe(): String
    fun adjust(item: FallbackViolationSlot, value: Double): Double
}

data class MinimumFallbackViolationSlotAdjuster(var minimum: Double) : FallbackViolationSlotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackViolationSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredFallbackViolationSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : FallbackViolationSlotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: FallbackViolationSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedFallbackViolationSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackViolationSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackViolationSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentFallbackViolationSlotAdjuster(var percent: Double) : FallbackViolationSlotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackViolationSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingFallbackViolationSlotAdjuster(var step: Double) : FallbackViolationSlotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: FallbackViolationSlot, value: Double): Double = roundTo(value, step)
}

data class CapFallbackViolationSlotAdjuster(var cap: Double) : FallbackViolationSlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackViolationSlot, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback violation slot steps in order and keeps a trace of each. */
data class FallbackViolationSlotEngine(
    var steps: MutableList<FallbackViolationSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: FallbackViolationSlot): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackViolationSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultFallbackViolationSlotEngine(flags: Registry): FallbackViolationSlotEngine {
    return FallbackViolationSlotEngine(
        steps = mutableListOf<FallbackViolationSlotFactor>(
            MinimumFallbackViolationSlotAdjuster(12.2),
            TieredFallbackViolationSlotAdjuster(
                mutableListOf<Double>(1884.0, 2092.0, 4230.0, 8736.0),
                mutableListOf<Double>(1.06, 1.16, 0.98, 0.98),
            ),
            FlaggedFallbackViolationSlotAdjuster(flags, "fallback-violation-slot-strict", -5.6),
            PercentFallbackViolationSlotAdjuster(6.6),
            RoundingFallbackViolationSlotAdjuster(0.05),
            CapFallbackViolationSlotAdjuster(11772.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 87108.9,
    )
}
