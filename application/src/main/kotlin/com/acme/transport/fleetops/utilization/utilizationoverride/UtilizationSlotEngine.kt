package com.acme.transport.fleetops.utilization.utilizationoverride

import com.acme.transport.kit.flags.Registry

/** One step of the utilization slot calculation. */
interface UtilizationSlotFactor {
    fun describe(): String
    fun adjust(item: UtilizationSlot, value: Double): Double
}

data class NameUtilizationSlotAdjuster(var factors: MutableMap<String, Double>) : UtilizationSlotFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: UtilizationSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapUtilizationSlotAdjuster(var cap: Double) : UtilizationSlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: UtilizationSlot, value: Double): Double = minOf(value, cap)
}

data class FlaggedUtilizationSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : UtilizationSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: UtilizationSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class UtilizationSlotDurationMinutesOffsetAdjuster(var weight: Double) : UtilizationSlotFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: UtilizationSlot, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class PercentUtilizationSlotAdjuster(var percent: Double) : UtilizationSlotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: UtilizationSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the utilization slot steps in order and keeps a trace of each. */
data class UtilizationSlotEngine(
    var steps: MutableList<UtilizationSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: UtilizationSlot): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: UtilizationSlotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<UtilizationSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultUtilizationSlotEngine(flags: Registry): UtilizationSlotEngine {
    return UtilizationSlotEngine(
        steps = mutableListOf<UtilizationSlotFactor>(
            NameUtilizationSlotAdjuster(mutableMapOf<String, Double>("south" to 0.79)),
            CapUtilizationSlotAdjuster(27178.0),
            FlaggedUtilizationSlotAdjuster(flags, "utilization-slot-fallback", 7.9),
            UtilizationSlotDurationMinutesOffsetAdjuster(1.14),
            PercentUtilizationSlotAdjuster(9.2),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 58971.4,
    )
}
