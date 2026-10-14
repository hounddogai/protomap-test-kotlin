package com.acme.transport.lastmile.locker.mapping

/** One step of the locker mapping calculation. */
interface LockerMappingFactor {
    fun describe(): String
    fun adjust(item: LockerMapping, value: Double): Double
}

data class PercentLockerMappingAdjuster(var percent: Double) : LockerMappingFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: LockerMapping, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredLockerMappingAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : LockerMappingFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: LockerMapping, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumLockerMappingAdjuster(var minimum: Double) : LockerMappingFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: LockerMapping, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the locker mapping steps in order and keeps a trace of each. */
data class LockerMappingEngine(
    var steps: MutableList<LockerMappingFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: LockerMapping): Double {
        var current = item.weightedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: LockerMappingFactor = steps[index]
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

    fun evaluateAll(items: MutableList<LockerMapping>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultLockerMappingEngine(): LockerMappingEngine {
    return LockerMappingEngine(
        steps = mutableListOf<LockerMappingFactor>(
            PercentLockerMappingAdjuster(-5.4),
            TieredLockerMappingAdjuster(
                mutableListOf<Double>(2217.0, 5057.0, 6005.0),
                mutableListOf<Double>(1.01, 0.84, 1.04),
            ),
            MinimumLockerMappingAdjuster(34.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.1,
        ceiling = 69319.2,
    )
}
