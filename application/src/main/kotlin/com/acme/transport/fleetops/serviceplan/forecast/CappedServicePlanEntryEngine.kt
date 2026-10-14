package com.acme.transport.fleetops.serviceplan.forecast

/** One step of the capped service plan entry calculation. */
interface CappedServicePlanEntryStep {
    fun describe(): String
    fun adjust(item: CappedServicePlanEntry, value: Double): Double
}

data class MinimumCappedServicePlanEntryAdjuster(var minimum: Double) : CappedServicePlanEntryStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedServicePlanEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CappedServicePlanEntryDiscountOffsetAdjuster(var weight: Double) : CappedServicePlanEntryStep {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: CappedServicePlanEntry, value: Double): Double = value + item.discount * weight
}

data class TieredCappedServicePlanEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedServicePlanEntryStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedServicePlanEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCappedServicePlanEntryAdjuster(var percent: Double) : CappedServicePlanEntryStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedServicePlanEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TitleCappedServicePlanEntryAdjuster(
    var factors: MutableMap<String, Double>,
) : CappedServicePlanEntryStep {
    override fun describe(): String = "by title"

    override fun adjust(item: CappedServicePlanEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapCappedServicePlanEntryAdjuster(var cap: Double) : CappedServicePlanEntryStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedServicePlanEntry, value: Double): Double = minOf(value, cap)
}

/** Runs the capped service plan entry steps in order and keeps a trace of each. */
data class CappedServicePlanEntryEngine(
    var steps: MutableList<CappedServicePlanEntryStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: CappedServicePlanEntry): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedServicePlanEntryStep = steps[index]
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

    fun evaluateAll(items: MutableList<CappedServicePlanEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultCappedServicePlanEntryEngine(): CappedServicePlanEntryEngine {
    return CappedServicePlanEntryEngine(
        steps = mutableListOf<CappedServicePlanEntryStep>(
            MinimumCappedServicePlanEntryAdjuster(46.7),
            CappedServicePlanEntryDiscountOffsetAdjuster(0.34),
            TieredCappedServicePlanEntryAdjuster(
                mutableListOf<Double>(550.0, 2254.0, 8635.0, 8698.0),
                mutableListOf<Double>(0.88, 1.15, 1.15, 1.11),
            ),
            PercentCappedServicePlanEntryAdjuster(-5.5),
            TitleCappedServicePlanEntryAdjuster(mutableMapOf<String, Double>("central" to 1.29)),
            CapCappedServicePlanEntryAdjuster(43912.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 89779.3,
    )
}
