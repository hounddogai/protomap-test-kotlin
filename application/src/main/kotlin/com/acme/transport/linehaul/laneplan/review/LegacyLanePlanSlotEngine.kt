package com.acme.transport.linehaul.laneplan.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the legacy lane plan slot calculation. */
interface LegacyLanePlanSlotAdjuster {
    fun describe(): String
    fun adjust(item: LegacyLanePlanSlot, value: Double): Double
}

data class NameLegacyLanePlanSlotAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyLanePlanSlotAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: LegacyLanePlanSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredLegacyLanePlanSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LegacyLanePlanSlotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LegacyLanePlanSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLegacyLanePlanSlotAdjuster(var minimum: Double) : LegacyLanePlanSlotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyLanePlanSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingLegacyLanePlanSlotAdjuster(var step: Double) : LegacyLanePlanSlotAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: LegacyLanePlanSlot, value: Double): Double = roundTo(value, step)
}

data class FlaggedLegacyLanePlanSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyLanePlanSlotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyLanePlanSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentLegacyLanePlanSlotAdjuster(var percent: Double) : LegacyLanePlanSlotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyLanePlanSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the legacy lane plan slot steps in order and keeps a trace of each. */
data class LegacyLanePlanSlotEngine(
    var steps: MutableList<LegacyLanePlanSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: LegacyLanePlanSlot): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyLanePlanSlotAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyLanePlanSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultLegacyLanePlanSlotEngine(flags: Registry): LegacyLanePlanSlotEngine {
    return LegacyLanePlanSlotEngine(
        steps = mutableListOf<LegacyLanePlanSlotAdjuster>(
            NameLegacyLanePlanSlotAdjuster(mutableMapOf<String, Double>("central" to 1.17)),
            TieredLegacyLanePlanSlotAdjuster(
                mutableListOf<Double>(956.0, 4920.0, 5273.0, 5453.0),
                mutableListOf<Double>(1.06, 1.11, 0.8, 0.97),
            ),
            MinimumLegacyLanePlanSlotAdjuster(13.3),
            RoundingLegacyLanePlanSlotAdjuster(1.0),
            FlaggedLegacyLanePlanSlotAdjuster(flags, "legacy-lane-plan-slot-scheduled", 0.7),
            PercentLegacyLanePlanSlotAdjuster(7.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 5073.7,
    )
}
