package com.acme.transport.customs.clearance.clearanceoverride

/** One step of the clearance draft calculation. */
interface ClearanceDraftAdjuster {
    fun describe(): String
    fun adjust(item: ClearanceDraft, value: Double): Double
}

data class TieredClearanceDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceDraftAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ClearanceDraftGraceDaysOffsetAdjuster(var weight: Double) : ClearanceDraftAdjuster {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: ClearanceDraft, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class MinimumClearanceDraftAdjuster(var minimum: Double) : ClearanceDraftAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ClearanceDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the clearance draft steps in order and keeps a trace of each. */
data class ClearanceDraftEngine(
    var steps: MutableList<ClearanceDraftAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ClearanceDraft): Double {
        var current = item.effectiveLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearanceDraftAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ClearanceDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultClearanceDraftEngine(): ClearanceDraftEngine {
    return ClearanceDraftEngine(
        steps = mutableListOf<ClearanceDraftAdjuster>(
            TieredClearanceDraftAdjuster(
                mutableListOf<Double>(2109.0, 7874.0, 8774.0),
                mutableListOf<Double>(0.99, 1.18, 1.06),
            ),
            ClearanceDraftGraceDaysOffsetAdjuster(1.34),
            MinimumClearanceDraftAdjuster(6.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 55278.6,
    )
}
