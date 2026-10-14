package com.acme.transport.customs.clearance.tier

/** One step of the clearance estimate calculation. */
interface ClearanceEstimateAdjuster {
    fun describe(): String
    fun adjust(item: ClearanceEstimate, value: Double): Double
}

data class ClearanceEstimateEtaMinutesOffsetAdjuster(var weight: Double) : ClearanceEstimateAdjuster {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: ClearanceEstimate, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class PercentClearanceEstimateAdjuster(var percent: Double) : ClearanceEstimateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ClearanceEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredClearanceEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceEstimateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelClearanceEstimateAdjuster(var factors: MutableMap<String, Double>) : ClearanceEstimateAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: ClearanceEstimate, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapClearanceEstimateAdjuster(var cap: Double) : ClearanceEstimateAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ClearanceEstimate, value: Double): Double = minOf(value, cap)
}

data class MinimumClearanceEstimateAdjuster(var minimum: Double) : ClearanceEstimateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ClearanceEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the clearance estimate steps in order and keeps a trace of each. */
data class ClearanceEstimateEngine(
    var steps: MutableList<ClearanceEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ClearanceEstimate): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearanceEstimateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ClearanceEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultClearanceEstimateEngine(): ClearanceEstimateEngine {
    return ClearanceEstimateEngine(
        steps = mutableListOf<ClearanceEstimateAdjuster>(
            ClearanceEstimateEtaMinutesOffsetAdjuster(0.49),
            PercentClearanceEstimateAdjuster(-16.9),
            TieredClearanceEstimateAdjuster(
                mutableListOf<Double>(1937.0, 6481.0, 7553.0, 8794.0),
                mutableListOf<Double>(1.05, 0.85, 1.06, 1.13),
            ),
            LabelClearanceEstimateAdjuster(
                mutableMapOf<String, Double>("north" to 0.84, "retail" to 1.35, "online" to 1.29),
            ),
            CapClearanceEstimateAdjuster(9164.0),
            MinimumClearanceEstimateAdjuster(40.6),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 16514.8,
    )
}
