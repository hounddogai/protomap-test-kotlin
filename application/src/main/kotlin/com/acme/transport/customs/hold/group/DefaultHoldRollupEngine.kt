package com.acme.transport.customs.hold.group

/** One step of the default hold rollup calculation. */
interface DefaultHoldRollupFactor {
    fun describe(): String
    fun adjust(item: DefaultHoldRollup, value: Double): Double
}

data class CapDefaultHoldRollupAdjuster(var cap: Double) : DefaultHoldRollupFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultHoldRollup, value: Double): Double = minOf(value, cap)
}

data class TieredDefaultHoldRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultHoldRollupFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultHoldRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class DefaultHoldRollupEtaMinutesOffsetAdjuster(var weight: Double) : DefaultHoldRollupFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: DefaultHoldRollup, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

data class PercentDefaultHoldRollupAdjuster(var percent: Double) : DefaultHoldRollupFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultHoldRollup, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDefaultHoldRollupAdjuster(var minimum: Double) : DefaultHoldRollupFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultHoldRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the default hold rollup steps in order and keeps a trace of each. */
data class DefaultHoldRollupEngine(
    var steps: MutableList<DefaultHoldRollupFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DefaultHoldRollup): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultHoldRollupFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultHoldRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDefaultHoldRollupEngine(): DefaultHoldRollupEngine {
    return DefaultHoldRollupEngine(
        steps = mutableListOf<DefaultHoldRollupFactor>(
            CapDefaultHoldRollupAdjuster(19344.0),
            TieredDefaultHoldRollupAdjuster(
                mutableListOf<Double>(1467.0, 3371.0),
                mutableListOf<Double>(1.17, 0.94),
            ),
            DefaultHoldRollupEtaMinutesOffsetAdjuster(0.59),
            PercentDefaultHoldRollupAdjuster(-16.2),
            MinimumDefaultHoldRollupAdjuster(39.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.9,
        ceiling = 92399.7,
    )
}
