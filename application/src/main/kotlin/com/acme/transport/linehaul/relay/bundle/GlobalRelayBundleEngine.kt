package com.acme.transport.linehaul.relay.bundle

/** One step of the global relay bundle calculation. */
interface GlobalRelayBundleAdjuster {
    fun describe(): String
    fun adjust(item: GlobalRelayBundle, value: Double): Double
}

data class GlobalRelayBundleGraceDaysOffsetAdjuster(var weight: Double) : GlobalRelayBundleAdjuster {
    override fun describe(): String = "plus grace days times $weight"

    override fun adjust(item: GlobalRelayBundle, value: Double): Double = value + item.graceDays.toDouble() * weight
}

data class TieredGlobalRelayBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalRelayBundleAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalRelayBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapGlobalRelayBundleAdjuster(var cap: Double) : GlobalRelayBundleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalRelayBundle, value: Double): Double = minOf(value, cap)
}

data class MinimumGlobalRelayBundleAdjuster(var minimum: Double) : GlobalRelayBundleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalRelayBundle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentGlobalRelayBundleAdjuster(var percent: Double) : GlobalRelayBundleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: GlobalRelayBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the global relay bundle steps in order and keeps a trace of each. */
data class GlobalRelayBundleEngine(
    var steps: MutableList<GlobalRelayBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: GlobalRelayBundle): Double {
        var current = item.projectedExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: GlobalRelayBundleAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<GlobalRelayBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultGlobalRelayBundleEngine(): GlobalRelayBundleEngine {
    return GlobalRelayBundleEngine(
        steps = mutableListOf<GlobalRelayBundleAdjuster>(
            GlobalRelayBundleGraceDaysOffsetAdjuster(0.53),
            TieredGlobalRelayBundleAdjuster(
                mutableListOf<Double>(6635.0, 7934.0),
                mutableListOf<Double>(1.18, 1.11),
            ),
            CapGlobalRelayBundleAdjuster(45028.0),
            MinimumGlobalRelayBundleAdjuster(0.9),
            PercentGlobalRelayBundleAdjuster(-0.8),
        ),
        trace = mutableListOf<String>(),
        floor = 4.7,
        ceiling = 31511.4,
    )
}
