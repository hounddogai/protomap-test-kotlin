package com.acme.transport.lastmile.locker.lockerquota

/** One step of the strict locker threshold calculation. */
interface StrictLockerThresholdStep {
    fun describe(): String
    fun adjust(item: StrictLockerThreshold, value: Double): Double
}

data class CountryStrictLockerThresholdAdjuster(
    var factors: MutableMap<String, Double>,
) : StrictLockerThresholdStep {
    override fun describe(): String = "by country"

    override fun adjust(item: StrictLockerThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredStrictLockerThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : StrictLockerThresholdStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: StrictLockerThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumStrictLockerThresholdAdjuster(var minimum: Double) : StrictLockerThresholdStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: StrictLockerThreshold, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class StrictLockerThresholdWeightOffsetAdjuster(var weight: Double) : StrictLockerThresholdStep {
    override fun describe(): String = "plus weight times $weight"

    override fun adjust(item: StrictLockerThreshold, value: Double): Double = value + item.weight * weight
}

/** Runs the strict locker threshold steps in order and keeps a trace of each. */
data class StrictLockerThresholdEngine(
    var steps: MutableList<StrictLockerThresholdStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictLockerThreshold): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictLockerThresholdStep = steps[index]
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

    fun evaluateAll(items: MutableList<StrictLockerThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictLockerThresholdEngine(): StrictLockerThresholdEngine {
    return StrictLockerThresholdEngine(
        steps = mutableListOf<StrictLockerThresholdStep>(
            CountryStrictLockerThresholdAdjuster(
                mutableMapOf<String, Double>("retail" to 0.76, "north" to 1.38, "east" to 0.92),
            ),
            TieredStrictLockerThresholdAdjuster(
                mutableListOf<Double>(838.0, 3984.0, 4703.0),
                mutableListOf<Double>(0.82, 0.96, 0.96),
            ),
            MinimumStrictLockerThresholdAdjuster(15.7),
            StrictLockerThresholdWeightOffsetAdjuster(0.58),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 78646.0,
    )
}
