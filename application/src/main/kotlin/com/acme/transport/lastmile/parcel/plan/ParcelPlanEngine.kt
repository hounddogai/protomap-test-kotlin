package com.acme.transport.lastmile.parcel.plan

/** One step of the parcel plan calculation. */
interface ParcelPlanStep {
    fun describe(): String
    fun adjust(item: ParcelPlan, value: Double): Double
}

data class MinimumParcelPlanAdjuster(var minimum: Double) : ParcelPlanStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ParcelPlan, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentParcelPlanAdjuster(var percent: Double) : ParcelPlanStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ParcelPlan, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapParcelPlanAdjuster(var cap: Double) : ParcelPlanStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ParcelPlan, value: Double): Double = minOf(value, cap)
}

data class TieredParcelPlanAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ParcelPlanStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ParcelPlan, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the parcel plan steps in order and keeps a trace of each. */
data class ParcelPlanEngine(
    var steps: MutableList<ParcelPlanStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ParcelPlan): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ParcelPlanStep = steps[index]
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

    fun evaluateAll(items: MutableList<ParcelPlan>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultParcelPlanEngine(): ParcelPlanEngine {
    return ParcelPlanEngine(
        steps = mutableListOf<ParcelPlanStep>(
            MinimumParcelPlanAdjuster(45.9),
            PercentParcelPlanAdjuster(10.6),
            CapParcelPlanAdjuster(18617.0),
            TieredParcelPlanAdjuster(
                mutableListOf<Double>(6493.0, 8574.0, 9368.0),
                mutableListOf<Double>(0.82, 1.18, 0.83),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 56734.8,
    )
}
