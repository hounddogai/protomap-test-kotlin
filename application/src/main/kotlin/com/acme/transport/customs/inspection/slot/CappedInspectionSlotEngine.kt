package com.acme.transport.customs.inspection.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped inspection slot calculation. */
interface CappedInspectionSlotFactor {
    fun describe(): String
    fun adjust(item: CappedInspectionSlot, value: Double): Double
}

data class FlaggedCappedInspectionSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedInspectionSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedInspectionSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCappedInspectionSlotAdjuster(var percent: Double) : CappedInspectionSlotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedInspectionSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingCappedInspectionSlotAdjuster(var step: Double) : CappedInspectionSlotFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedInspectionSlot, value: Double): Double = roundTo(value, step)
}

data class CappedInspectionSlotScoreOffsetAdjuster(var weight: Double) : CappedInspectionSlotFactor {
    override fun describe(): String = "plus score times $weight"

    override fun adjust(item: CappedInspectionSlot, value: Double): Double = value + item.score * weight
}

data class MinimumCappedInspectionSlotAdjuster(var minimum: Double) : CappedInspectionSlotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedInspectionSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCappedInspectionSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedInspectionSlotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedInspectionSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the capped inspection slot steps in order and keeps a trace of each. */
data class CappedInspectionSlotEngine(
    var steps: MutableList<CappedInspectionSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CappedInspectionSlot): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedInspectionSlotFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedInspectionSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCappedInspectionSlotEngine(flags: Registry): CappedInspectionSlotEngine {
    return CappedInspectionSlotEngine(
        steps = mutableListOf<CappedInspectionSlotFactor>(
            FlaggedCappedInspectionSlotAdjuster(flags, "capped-inspection-slot-strict", 6.2),
            PercentCappedInspectionSlotAdjuster(11.2),
            RoundingCappedInspectionSlotAdjuster(5.0),
            CappedInspectionSlotScoreOffsetAdjuster(0.38),
            MinimumCappedInspectionSlotAdjuster(15.2),
            TieredCappedInspectionSlotAdjuster(
                mutableListOf<Double>(2135.0, 7666.0),
                mutableListOf<Double>(0.92, 1.17),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.9,
        ceiling = 79647.0,
    )
}
