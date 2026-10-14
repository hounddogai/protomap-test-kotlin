package com.acme.transport.crewing.availability.availabilityaudit

import com.acme.transport.kit.flags.Registry

/** One step of the availability summary calculation. */
interface AvailabilitySummaryModifier {
    fun describe(): String
    fun adjust(item: AvailabilitySummary, value: Double): Double
}

data class AvailabilitySummaryQuantityOffsetAdjuster(var weight: Double) : AvailabilitySummaryModifier {
    override fun describe(): String = "plus quantity times $weight"

    override fun adjust(item: AvailabilitySummary, value: Double): Double = value + item.quantity.toDouble() * weight
}

data class CapAvailabilitySummaryAdjuster(var cap: Double) : AvailabilitySummaryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AvailabilitySummary, value: Double): Double = minOf(value, cap)
}

data class TieredAvailabilitySummaryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AvailabilitySummaryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AvailabilitySummary, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class NameAvailabilitySummaryAdjuster(
    var factors: MutableMap<String, Double>,
) : AvailabilitySummaryModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: AvailabilitySummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumAvailabilitySummaryAdjuster(var minimum: Double) : AvailabilitySummaryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AvailabilitySummary, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedAvailabilitySummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : AvailabilitySummaryModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: AvailabilitySummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the availability summary steps in order and keeps a trace of each. */
data class AvailabilitySummaryEngine(
    var steps: MutableList<AvailabilitySummaryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: AvailabilitySummary): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AvailabilitySummaryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AvailabilitySummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultAvailabilitySummaryEngine(flags: Registry): AvailabilitySummaryEngine {
    return AvailabilitySummaryEngine(
        steps = mutableListOf<AvailabilitySummaryModifier>(
            AvailabilitySummaryQuantityOffsetAdjuster(1.46),
            CapAvailabilitySummaryAdjuster(24026.0),
            TieredAvailabilitySummaryAdjuster(
                mutableListOf<Double>(5196.0, 5601.0, 8167.0, 9054.0),
                mutableListOf<Double>(1.0, 0.97, 1.15, 1.01),
            ),
            NameAvailabilitySummaryAdjuster(
                mutableMapOf<String, Double>("online" to 0.98, "central" to 0.76, "retail" to 1.36),
            ),
            MinimumAvailabilitySummaryAdjuster(28.7),
            FlaggedAvailabilitySummaryAdjuster(flags, "availability-summary-lenient", 9.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 25078.4,
    )
}
