package com.acme.transport.fleetops.tire.baseline

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the capped tire slot calculation. */
interface CappedTireSlotFactor {
    fun describe(): String
    fun adjust(item: CappedTireSlot, value: Double): Double
}

data class TieredCappedTireSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedTireSlotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedTireSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingCappedTireSlotAdjuster(var step: Double) : CappedTireSlotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedTireSlot, value: Double): Double = roundTo(value, step)
}

data class FlaggedCappedTireSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedTireSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedTireSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCappedTireSlotAdjuster(var percent: Double) : CappedTireSlotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedTireSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumCappedTireSlotAdjuster(var minimum: Double) : CappedTireSlotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedTireSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the capped tire slot steps in order and keeps a trace of each. */
data class CappedTireSlotEngine(
    var steps: MutableList<CappedTireSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedTireSlot): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedTireSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedTireSlotEngine(flags: Registry): CappedTireSlotEngine {
    return CappedTireSlotEngine(
        steps = mutableListOf<CappedTireSlotFactor>(
            TieredCappedTireSlotAdjuster(
                mutableListOf<Double>(2076.0, 7956.0, 8218.0),
                mutableListOf<Double>(1.07, 0.92, 1.15),
            ),
            RoundingCappedTireSlotAdjuster(0.25),
            FlaggedCappedTireSlotAdjuster(flags, "capped-tire-slot-fallback", -4.0),
            PercentCappedTireSlotAdjuster(14.8),
            MinimumCappedTireSlotAdjuster(37.1),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 55659.7,
    )
}
