package com.acme.transport.terminal.crossdock.schedule

/** One step of the regional cross dock quota calculation. */
interface RegionalCrossDockQuotaModifier {
    fun describe(): String
    fun adjust(item: RegionalCrossDockQuota, value: Double): Double
}

data class LabelRegionalCrossDockQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalCrossDockQuotaModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: RegionalCrossDockQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRegionalCrossDockQuotaAdjuster(var minimum: Double) : RegionalCrossDockQuotaModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalCrossDockQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredRegionalCrossDockQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalCrossDockQuotaModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalCrossDockQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRegionalCrossDockQuotaAdjuster(var cap: Double) : RegionalCrossDockQuotaModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RegionalCrossDockQuota, value: Double): Double = minOf(value, cap)
}

/** Runs the regional cross dock quota steps in order and keeps a trace of each. */
data class RegionalCrossDockQuotaEngine(
    var steps: MutableList<RegionalCrossDockQuotaModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RegionalCrossDockQuota): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalCrossDockQuotaModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalCrossDockQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRegionalCrossDockQuotaEngine(): RegionalCrossDockQuotaEngine {
    return RegionalCrossDockQuotaEngine(
        steps = mutableListOf<RegionalCrossDockQuotaModifier>(
            LabelRegionalCrossDockQuotaAdjuster(mutableMapOf<String, Double>("central" to 1.29, "west" to 1.22)),
            MinimumRegionalCrossDockQuotaAdjuster(48.1),
            TieredRegionalCrossDockQuotaAdjuster(
                mutableListOf<Double>(3441.0, 7118.0),
                mutableListOf<Double>(1.16, 1.06),
            ),
            CapRegionalCrossDockQuotaAdjuster(27858.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.5,
        ceiling = 28479.9,
    )
}
