package com.acme.transport.emissions.fuelburn.adjustment

import com.acme.transport.kit.flags.Registry

/** One step of the legacy fuel burn entry calculation. */
interface LegacyFuelBurnEntryFactor {
    fun describe(): String
    fun adjust(item: LegacyFuelBurnEntry, value: Double): Double
}

data class RegionLegacyFuelBurnEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : LegacyFuelBurnEntryFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: LegacyFuelBurnEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedLegacyFuelBurnEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : LegacyFuelBurnEntryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: LegacyFuelBurnEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumLegacyFuelBurnEntryAdjuster(var minimum: Double) : LegacyFuelBurnEntryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyFuelBurnEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LegacyFuelBurnEntryDurationMinutesOffsetAdjuster(var weight: Double) : LegacyFuelBurnEntryFactor {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: LegacyFuelBurnEntry, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

/** Runs the legacy fuel burn entry steps in order and keeps a trace of each. */
data class LegacyFuelBurnEntryEngine(
    var steps: MutableList<LegacyFuelBurnEntryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: LegacyFuelBurnEntry): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyFuelBurnEntryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyFuelBurnEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultLegacyFuelBurnEntryEngine(flags: Registry): LegacyFuelBurnEntryEngine {
    return LegacyFuelBurnEntryEngine(
        steps = mutableListOf<LegacyFuelBurnEntryFactor>(
            RegionLegacyFuelBurnEntryAdjuster(
                mutableMapOf<String, Double>("north" to 1.06, "east" to 0.88, "online" to 1.38),
            ),
            FlaggedLegacyFuelBurnEntryAdjuster(flags, "legacy-fuel-burn-entry-seasonal", -9.9),
            MinimumLegacyFuelBurnEntryAdjuster(1.4),
            LegacyFuelBurnEntryDurationMinutesOffsetAdjuster(1.09),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 35131.8,
    )
}
