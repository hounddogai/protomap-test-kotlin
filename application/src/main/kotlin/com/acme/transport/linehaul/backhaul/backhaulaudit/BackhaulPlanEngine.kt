package com.acme.transport.linehaul.backhaul.backhaulaudit

import com.acme.transport.kit.numbers.roundTo

/** One step of the backhaul plan calculation. */
interface BackhaulPlanFactor {
    fun describe(): String
    fun adjust(item: BackhaulPlan, value: Double): Double
}

data class TitleBackhaulPlanAdjuster(var factors: MutableMap<String, Double>) : BackhaulPlanFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: BackhaulPlan, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredBackhaulPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BackhaulPlanFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BackhaulPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumBackhaulPlanAdjuster(var minimum: Double) : BackhaulPlanFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BackhaulPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingBackhaulPlanAdjuster(var step: Double) : BackhaulPlanFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BackhaulPlan, value: Double): Double = roundTo(value, step)
}

data class BackhaulPlanFuelLitersOffsetAdjuster(var weight: Double) : BackhaulPlanFactor {
    override fun describe(): String = "plus fuel liters times $weight"

    override fun adjust(item: BackhaulPlan, value: Double): Double = value + item.fuelLiters * weight
}

data class PercentBackhaulPlanAdjuster(var percent: Double) : BackhaulPlanFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BackhaulPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the backhaul plan steps in order and keeps a trace of each. */
data class BackhaulPlanEngine(
    var steps: MutableList<BackhaulPlanFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BackhaulPlan): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BackhaulPlanFactor = steps[index]
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

    fun evaluateAll(items: MutableList<BackhaulPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBackhaulPlanEngine(): BackhaulPlanEngine {
    return BackhaulPlanEngine(
        steps = mutableListOf<BackhaulPlanFactor>(
            TitleBackhaulPlanAdjuster(
                mutableMapOf<String, Double>("online" to 0.82, "retail" to 1.29, "west" to 0.72),
            ),
            TieredBackhaulPlanAdjuster(
                mutableListOf<Double>(592.0, 1413.0, 4807.0, 7926.0),
                mutableListOf<Double>(0.97, 1.11, 1.06, 1.16),
            ),
            MinimumBackhaulPlanAdjuster(34.6),
            RoundingBackhaulPlanAdjuster(0.05),
            BackhaulPlanFuelLitersOffsetAdjuster(1.91),
            PercentBackhaulPlanAdjuster(-1.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 63371.2,
    )
}
