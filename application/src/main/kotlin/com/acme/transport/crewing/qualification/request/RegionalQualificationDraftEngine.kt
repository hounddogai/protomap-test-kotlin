package com.acme.transport.crewing.qualification.request

/** One step of the regional qualification draft calculation. */
interface RegionalQualificationDraftModifier {
    fun describe(): String
    fun adjust(item: RegionalQualificationDraft, value: Double): Double
}

data class NameRegionalQualificationDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalQualificationDraftModifier {
    override fun describe(): String = "by name"

    override fun adjust(item: RegionalQualificationDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RegionalQualificationDraftDurationMinutesOffsetAdjuster(
    var weight: Double,
) : RegionalQualificationDraftModifier {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: RegionalQualificationDraft, value: Double): Double {
        return value + item.durationMinutes.toDouble() * weight
    }
}

data class CapRegionalQualificationDraftAdjuster(var cap: Double) : RegionalQualificationDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalQualificationDraft, value: Double): Double = minOf(value, cap)
}

data class TieredRegionalQualificationDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalQualificationDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalQualificationDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumRegionalQualificationDraftAdjuster(var minimum: Double) : RegionalQualificationDraftModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalQualificationDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the regional qualification draft steps in order and keeps a trace of each. */
data class RegionalQualificationDraftEngine(
    var steps: MutableList<RegionalQualificationDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalQualificationDraft): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalQualificationDraftModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalQualificationDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalQualificationDraftEngine(): RegionalQualificationDraftEngine {
    return RegionalQualificationDraftEngine(
        steps = mutableListOf<RegionalQualificationDraftModifier>(
            NameRegionalQualificationDraftAdjuster(
                mutableMapOf<String, Double>("online" to 1.04, "east" to 1.17, "north" to 1.03),
            ),
            RegionalQualificationDraftDurationMinutesOffsetAdjuster(1.73),
            CapRegionalQualificationDraftAdjuster(46256.0),
            TieredRegionalQualificationDraftAdjuster(
                mutableListOf<Double>(3309.0, 7080.0, 8796.0),
                mutableListOf<Double>(0.87, 0.83, 0.92),
            ),
            MinimumRegionalQualificationDraftAdjuster(47.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.6,
        ceiling = 8312.0,
    )
}
