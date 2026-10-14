package com.acme.transport.safety.violation.violationslot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the regional violation slot calculation. */
interface RegionalViolationSlotAdjuster {
    fun describe(): String
    fun adjust(item: RegionalViolationSlot, value: Double): Double
}

data class MinimumRegionalViolationSlotAdjuster(var minimum: Double) : RegionalViolationSlotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalViolationSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRegionalViolationSlotAdjuster(var percent: Double) : RegionalViolationSlotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalViolationSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ChannelRegionalViolationSlotAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalViolationSlotAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: RegionalViolationSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedRegionalViolationSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RegionalViolationSlotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RegionalViolationSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the regional violation slot steps in order and keeps a trace of each. */
data class RegionalViolationSlotEngine(
    var steps: MutableList<RegionalViolationSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalViolationSlot): Double {
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

    fun evaluateAll(items: MutableList<RegionalViolationSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalViolationSlotEngine(flags: Registry): RegionalViolationSlotEngine {
    return RegionalViolationSlotEngine(
        steps = mutableListOf<RegionalViolationSlotAdjuster>(
            MinimumRegionalViolationSlotAdjuster(20.0),
            PercentRegionalViolationSlotAdjuster(-19.6),
            ChannelRegionalViolationSlotAdjuster(mutableMapOf<String, Double>("west" to 0.71)),
            FlaggedRegionalViolationSlotAdjuster(flags, "regional-violation-slot-regional", -7.5),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 63030.7,
    )
}
