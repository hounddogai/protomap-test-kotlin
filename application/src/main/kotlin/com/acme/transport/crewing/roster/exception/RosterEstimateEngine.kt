package com.acme.transport.crewing.roster.exception

/** One step of the roster estimate calculation. */
interface RosterEstimateFactor {
    fun describe(): String
    fun adjust(item: RosterEstimate, value: Double): Double
}

data class MinimumRosterEstimateAdjuster(var minimum: Double) : RosterEstimateFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RosterEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRosterEstimateAdjuster(var percent: Double) : RosterEstimateFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RosterEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RosterEstimateOdometerOffsetAdjuster(var weight: Double) : RosterEstimateFactor {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: RosterEstimate, value: Double): Double = value + item.odometer.toDouble() * weight
}

/** Runs the roster estimate steps in order and keeps a trace of each. */
data class RosterEstimateEngine(
    var steps: MutableList<RosterEstimateFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RosterEstimate): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RosterEstimateFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RosterEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRosterEstimateEngine(): RosterEstimateEngine {
    return RosterEstimateEngine(
        steps = mutableListOf<RosterEstimateFactor>(
            MinimumRosterEstimateAdjuster(8.6),
            PercentRosterEstimateAdjuster(22.0),
            RosterEstimateOdometerOffsetAdjuster(1.54),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 47013.3,
    )
}
