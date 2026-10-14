package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

/** One step of the parcel estimate calculation. */
interface ParcelEstimateAdjuster {
    fun describe(): String
    fun adjust(item: ParcelEstimate, value: Double): Double
}

data class TieredParcelEstimateAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ParcelEstimateAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ParcelEstimate, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentParcelEstimateAdjuster(var percent: Double) : ParcelEstimateAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ParcelEstimate, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumParcelEstimateAdjuster(var minimum: Double) : ParcelEstimateAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ParcelEstimate, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the parcel estimate steps in order and keeps a trace of each. */
data class ParcelEstimateEngine(
    var steps: MutableList<ParcelEstimateAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ParcelEstimate): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ParcelEstimateAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<ParcelEstimate>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultParcelEstimateEngine(): ParcelEstimateEngine {
    return ParcelEstimateEngine(
        steps = mutableListOf<ParcelEstimateAdjuster>(
            TieredParcelEstimateAdjuster(
                mutableListOf<Double>(6135.0, 6382.0, 6674.0, 8277.0),
                mutableListOf<Double>(0.87, 1.18, 1.09, 1.08),
            ),
            PercentParcelEstimateAdjuster(-6.0),
            MinimumParcelEstimateAdjuster(25.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 20054.7,
    )
}
