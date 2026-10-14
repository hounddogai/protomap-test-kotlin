package com.acme.transport.fleetops.defect.policy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the strict defect slot calculation. */
interface StrictDefectSlotAdjuster {
    fun describe(): String
    fun adjust(item: StrictDefectSlot, value: Double): Double
}

data class MinimumStrictDefectSlotAdjuster(var minimum: Double) : StrictDefectSlotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictDefectSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedStrictDefectSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : StrictDefectSlotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: StrictDefectSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingStrictDefectSlotAdjuster(var step: Double) : StrictDefectSlotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: StrictDefectSlot, value: Double): Double = roundTo(value, step)
}

data class TieredStrictDefectSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictDefectSlotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictDefectSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapStrictDefectSlotAdjuster(var cap: Double) : StrictDefectSlotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictDefectSlot, value: Double): Double = minOf(value, cap)
}

data class StrictDefectSlotStopCountOffsetAdjuster(var weight: Double) : StrictDefectSlotAdjuster {
    override fun describe(): String = "plus stop count times $weight"

    override fun adjust(item: StrictDefectSlot, value: Double): Double = value + item.stopCount.toDouble() * weight
}

/** Runs the strict defect slot steps in order and keeps a trace of each. */
data class StrictDefectSlotEngine(
    var steps: MutableList<StrictDefectSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: StrictDefectSlot): Double {
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

    fun evaluateAll(items: MutableList<StrictDefectSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultStrictDefectSlotEngine(flags: Registry): StrictDefectSlotEngine {
    return StrictDefectSlotEngine(
        steps = mutableListOf<StrictDefectSlotAdjuster>(
            MinimumStrictDefectSlotAdjuster(7.6),
            FlaggedStrictDefectSlotAdjuster(flags, "strict-defect-slot-adaptive", -0.2),
            RoundingStrictDefectSlotAdjuster(5.0),
            TieredStrictDefectSlotAdjuster(
                mutableListOf<Double>(1497.0, 1819.0, 5871.0, 7081.0),
                mutableListOf<Double>(0.96, 1.09, 0.88, 0.99),
            ),
            CapStrictDefectSlotAdjuster(22380.0),
            StrictDefectSlotStopCountOffsetAdjuster(0.61),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 27103.0,
    )
}
