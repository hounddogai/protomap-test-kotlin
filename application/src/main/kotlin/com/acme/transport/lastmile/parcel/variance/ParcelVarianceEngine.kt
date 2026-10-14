package com.acme.transport.lastmile.parcel.variance

/** One step of the parcel variance calculation. */
interface ParcelVarianceStep {
    fun describe(): String
    fun adjust(item: ParcelVariance, value: Double): Double
}

data class CapParcelVarianceAdjuster(var cap: Double) : ParcelVarianceStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ParcelVariance, value: Double): Double = minOf(value, cap)
}

data class PercentParcelVarianceAdjuster(var percent: Double) : ParcelVarianceStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ParcelVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumParcelVarianceAdjuster(var minimum: Double) : ParcelVarianceStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ParcelVariance, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the parcel variance steps in order and keeps a trace of each. */
data class ParcelVarianceEngine(
    var steps: MutableList<ParcelVarianceStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ParcelVariance): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ParcelVarianceStep = steps[index]
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

    fun evaluateAll(items: MutableList<ParcelVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultParcelVarianceEngine(): ParcelVarianceEngine {
    return ParcelVarianceEngine(
        steps = mutableListOf<ParcelVarianceStep>(
            CapParcelVarianceAdjuster(39582.0),
            PercentParcelVarianceAdjuster(17.6),
            MinimumParcelVarianceAdjuster(20.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 79801.5,
    )
}
