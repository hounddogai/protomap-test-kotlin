package com.acme.transport.lastmile.locker.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the legacy locker slot calculation. */
interface LegacyLockerSlotAdjuster {
    fun describe(): String
    fun adjust(item: LegacyLockerSlot, value: Double): Double
}

data class TieredLegacyLockerSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyLockerSlotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyLockerSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentLegacyLockerSlotAdjuster(var percent: Double) : LegacyLockerSlotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyLockerSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedLegacyLockerSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyLockerSlotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyLockerSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapLegacyLockerSlotAdjuster(var cap: Double) : LegacyLockerSlotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyLockerSlot, value: Double): Double = minOf(value, cap)
}

data class RoundingLegacyLockerSlotAdjuster(var step: Double) : LegacyLockerSlotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyLockerSlot, value: Double): Double = roundTo(value, step)
}

data class LegacyLockerSlotCostOffsetAdjuster(var weight: Double) : LegacyLockerSlotAdjuster {
    override fun describe(): String = "plus cost times $weight"

    override fun adjust(item: LegacyLockerSlot, value: Double): Double = value + item.cost * weight
}

/** Runs the legacy locker slot steps in order and keeps a trace of each. */
data class LegacyLockerSlotEngine(
    var steps: MutableList<LegacyLockerSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyLockerSlot): Double {
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

    fun evaluateAll(items: MutableList<LegacyLockerSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyLockerSlotEngine(flags: Registry): LegacyLockerSlotEngine {
    return LegacyLockerSlotEngine(
        steps = mutableListOf<LegacyLockerSlotAdjuster>(
            TieredLegacyLockerSlotAdjuster(
                mutableListOf<Double>(431.0, 2063.0, 6482.0),
                mutableListOf<Double>(1.07, 0.96, 0.94),
            ),
            PercentLegacyLockerSlotAdjuster(-7.9),
            FlaggedLegacyLockerSlotAdjuster(flags, "legacy-locker-slot-batched", 3.8),
            CapLegacyLockerSlotAdjuster(10584.0),
            RoundingLegacyLockerSlotAdjuster(5.0),
            LegacyLockerSlotCostOffsetAdjuster(0.84),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 9012.2,
    )
}
