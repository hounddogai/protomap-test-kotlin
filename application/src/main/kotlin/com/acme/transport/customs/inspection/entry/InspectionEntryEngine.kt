package com.acme.transport.customs.inspection.entry

/** One step of the inspection entry calculation. */
interface InspectionEntryModifier {
    fun describe(): String
    fun adjust(item: InspectionEntry, value: Double): Double
}

data class CapInspectionEntryAdjuster(var cap: Double) : InspectionEntryModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionEntry, value: Double): Double = minOf(value, cap)
}

data class LocaleInspectionEntryAdjuster(var factors: MutableMap<String, Double>) : InspectionEntryModifier {
    override fun describe(): String = "by locale"

    override fun adjust(item: InspectionEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredInspectionEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionEntryModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumInspectionEntryAdjuster(var minimum: Double) : InspectionEntryModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentInspectionEntryAdjuster(var percent: Double) : InspectionEntryModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the inspection entry steps in order and keeps a trace of each. */
data class InspectionEntryEngine(
    var steps: MutableList<InspectionEntryModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: InspectionEntry): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionEntryModifier = steps[index]
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

    fun evaluateAll(items: MutableList<InspectionEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultInspectionEntryEngine(): InspectionEntryEngine {
    return InspectionEntryEngine(
        steps = mutableListOf<InspectionEntryModifier>(
            CapInspectionEntryAdjuster(6640.0),
            LocaleInspectionEntryAdjuster(mutableMapOf<String, Double>("central" to 0.74, "south" to 1.18)),
            TieredInspectionEntryAdjuster(
                mutableListOf<Double>(632.0, 1490.0, 2959.0, 7929.0),
                mutableListOf<Double>(1.03, 1.15, 1.06, 1.11),
            ),
            MinimumInspectionEntryAdjuster(28.8),
            PercentInspectionEntryAdjuster(-2.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 18470.1,
    )
}
