package com.acme.transport.safety.scorecard.entry

/** One step of the tiered scorecard entry calculation. */
interface TieredScorecardEntryFactor {
    fun describe(): String
    fun adjust(item: TieredScorecardEntry, value: Double): Double
}

data class TieredScorecardEntryRevisionOffsetAdjuster(var weight: Double) : TieredScorecardEntryFactor {
    override fun describe(): String = "plus revision times $weight"

    override fun adjust(item: TieredScorecardEntry, value: Double): Double = value + item.revision.toDouble() * weight
}

data class NameTieredScorecardEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : TieredScorecardEntryFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: TieredScorecardEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredTieredScorecardEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : TieredScorecardEntryFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: TieredScorecardEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the tiered scorecard entry steps in order and keeps a trace of each. */
data class TieredScorecardEntryEngine(
    var steps: MutableList<TieredScorecardEntryFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: TieredScorecardEntry): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: TieredScorecardEntryFactor = steps[index]
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

    fun evaluateAll(items: MutableList<TieredScorecardEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultTieredScorecardEntryEngine(): TieredScorecardEntryEngine {
    return TieredScorecardEntryEngine(
        steps = mutableListOf<TieredScorecardEntryFactor>(
            TieredScorecardEntryRevisionOffsetAdjuster(0.1),
            NameTieredScorecardEntryAdjuster(mutableMapOf<String, Double>("central" to 0.88, "online" to 0.83)),
            TieredTieredScorecardEntryAdjuster(
                mutableListOf<Double>(1513.0, 3185.0, 3677.0),
                mutableListOf<Double>(1.14, 1.07, 1.13),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.8,
        ceiling = 14667.8,
    )
}
