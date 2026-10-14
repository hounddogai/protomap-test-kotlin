package com.acme.transport.fleetops.serviceplan.planprofile

/** One step of the adaptive service plan record calculation. */
interface AdaptiveServicePlanRecordStep {
    fun describe(): String
    fun adjust(item: AdaptiveServicePlanRecord, value: Double): Double
}

data class MinimumAdaptiveServicePlanRecordAdjuster(var minimum: Double) : AdaptiveServicePlanRecordStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveServicePlanRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapAdaptiveServicePlanRecordAdjuster(var cap: Double) : AdaptiveServicePlanRecordStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveServicePlanRecord, value: Double): Double = minOf(value, cap)
}

data class TieredAdaptiveServicePlanRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveServicePlanRecordStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveServicePlanRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the adaptive service plan record steps in order and keeps a trace of each. */
data class AdaptiveServicePlanRecordEngine(
    var steps: MutableList<AdaptiveServicePlanRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: AdaptiveServicePlanRecord): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveServicePlanRecordStep = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveServicePlanRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultAdaptiveServicePlanRecordEngine(): AdaptiveServicePlanRecordEngine {
    return AdaptiveServicePlanRecordEngine(
        steps = mutableListOf<AdaptiveServicePlanRecordStep>(
            MinimumAdaptiveServicePlanRecordAdjuster(34.4),
            CapAdaptiveServicePlanRecordAdjuster(35520.0),
            TieredAdaptiveServicePlanRecordAdjuster(
                mutableListOf<Double>(1052.0, 6736.0),
                mutableListOf<Double>(0.93, 0.87),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 90396.8,
    )
}
