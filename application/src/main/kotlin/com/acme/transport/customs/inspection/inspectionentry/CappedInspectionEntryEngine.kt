package com.acme.transport.customs.inspection.inspectionentry

import com.acme.transport.kit.flags.Registry

/** One step of the capped inspection entry calculation. */
interface CappedInspectionEntryFactor {
    fun describe(): String
    fun adjust(item: CappedInspectionEntry, value: Double): Double
}

data class CategoryCappedInspectionEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedInspectionEntryFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: CappedInspectionEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumCappedInspectionEntryAdjuster(var minimum: Double) : CappedInspectionEntryFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedInspectionEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredCappedInspectionEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedInspectionEntryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedInspectionEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedCappedInspectionEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedInspectionEntryFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedInspectionEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentCappedInspectionEntryAdjuster(var percent: Double) : CappedInspectionEntryFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedInspectionEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CappedInspectionEntryOdometerOffsetAdjuster(var weight: Double) : CappedInspectionEntryFactor {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: CappedInspectionEntry, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

/** Runs the capped inspection entry steps in order and keeps a trace of each. */
data class CappedInspectionEntryEngine(
    var steps: MutableList<CappedInspectionEntryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedInspectionEntry): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedInspectionEntryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<CappedInspectionEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedInspectionEntryEngine(flags: Registry): CappedInspectionEntryEngine {
    return CappedInspectionEntryEngine(
        steps = mutableListOf<CappedInspectionEntryFactor>(
            CategoryCappedInspectionEntryAdjuster(
                mutableMapOf<String, Double>("online" to 1.05, "east" to 1.03),
            ),
            MinimumCappedInspectionEntryAdjuster(36.6),
            TieredCappedInspectionEntryAdjuster(
                mutableListOf<Double>(1062.0, 1108.0),
                mutableListOf<Double>(1.05, 0.92),
            ),
            FlaggedCappedInspectionEntryAdjuster(flags, "capped-inspection-entry-seasonal", 4.9),
            PercentCappedInspectionEntryAdjuster(5.3),
            CappedInspectionEntryOdometerOffsetAdjuster(0.26),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 95308.8,
    )
}
