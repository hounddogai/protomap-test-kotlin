package com.acme.transport.network.hub.milestone

/** One step of the legacy hub slot calculation. */
interface LegacyHubSlotFactor {
    fun describe(): String
    fun adjust(item: LegacyHubSlot, value: Double): Double
}

data class CapLegacyHubSlotAdjuster(var cap: Double) : LegacyHubSlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: LegacyHubSlot, value: Double): Double = minOf(value, cap)
}

data class MinimumLegacyHubSlotAdjuster(var minimum: Double) : LegacyHubSlotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LegacyHubSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentLegacyHubSlotAdjuster(var percent: Double) : LegacyHubSlotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LegacyHubSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CountryLegacyHubSlotAdjuster(var factors: MutableMap<String, Double>) : LegacyHubSlotFactor {
    override fun describe(): String = "by country"

    override fun adjust(item: LegacyHubSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class LegacyHubSlotEndsAtOffsetAdjuster(var weight: Double) : LegacyHubSlotFactor {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: LegacyHubSlot, value: Double): Double = value + item.endsAt.toDouble() * weight
}

/** Runs the legacy hub slot steps in order and keeps a trace of each. */
data class LegacyHubSlotEngine(
    var steps: MutableList<LegacyHubSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LegacyHubSlot): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LegacyHubSlotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LegacyHubSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLegacyHubSlotEngine(): LegacyHubSlotEngine {
    return LegacyHubSlotEngine(
        steps = mutableListOf<LegacyHubSlotFactor>(
            CapLegacyHubSlotAdjuster(22895.0),
            MinimumLegacyHubSlotAdjuster(29.2),
            PercentLegacyHubSlotAdjuster(-19.1),
            CountryLegacyHubSlotAdjuster(mutableMapOf<String, Double>("east" to 1.35)),
            LegacyHubSlotEndsAtOffsetAdjuster(1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 56454.7,
    )
}
