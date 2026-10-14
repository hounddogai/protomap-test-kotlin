package com.acme.transport.lastmile.parcel.slot

import com.acme.transport.kit.flags.Registry

/** One step of the regional parcel slot calculation. */
interface RegionalParcelSlotFactor {
    fun describe(): String
    fun adjust(item: RegionalParcelSlot, value: Double): Double
}

data class FlaggedRegionalParcelSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalParcelSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalParcelSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRegionalParcelSlotAdjuster(var cap: Double) : RegionalParcelSlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalParcelSlot, value: Double): Double = minOf(value, cap)
}

data class RegionalParcelSlotAmountOffsetAdjuster(var weight: Double) : RegionalParcelSlotFactor {
    override fun describe(): String = "plus amount times $weight"

    override fun adjust(item: RegionalParcelSlot, value: Double): Double = value + item.amount * weight
}

data class PercentRegionalParcelSlotAdjuster(var percent: Double) : RegionalParcelSlotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalParcelSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRegionalParcelSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalParcelSlotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalParcelSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ChannelRegionalParcelSlotAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalParcelSlotFactor {
    override fun describe(): String = "by channel"

    override fun adjust(item: RegionalParcelSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional parcel slot steps in order and keeps a trace of each. */
data class RegionalParcelSlotEngine(
    var steps: MutableList<RegionalParcelSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RegionalParcelSlot): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalParcelSlotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalParcelSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRegionalParcelSlotEngine(flags: Registry): RegionalParcelSlotEngine {
    return RegionalParcelSlotEngine(
        steps = mutableListOf<RegionalParcelSlotFactor>(
            FlaggedRegionalParcelSlotAdjuster(flags, "regional-parcel-slot-strict", -3.5),
            CapRegionalParcelSlotAdjuster(1364.0),
            RegionalParcelSlotAmountOffsetAdjuster(1.49),
            PercentRegionalParcelSlotAdjuster(3.5),
            TieredRegionalParcelSlotAdjuster(
                mutableListOf<Double>(176.0, 3787.0),
                mutableListOf<Double>(1.03, 0.99),
            ),
            ChannelRegionalParcelSlotAdjuster(
                mutableMapOf<String, Double>("central" to 1.29, "retail" to 1.25, "north" to 1.11),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 44621.9,
    )
}
