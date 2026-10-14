package com.acme.transport.fleetops.tire.tirewindow

/** One step of the default tire quota calculation. */
interface DefaultTireQuotaFactor {
    fun describe(): String
    fun adjust(item: DefaultTireQuota, value: Double): Double
}

data class PercentDefaultTireQuotaAdjuster(var percent: Double) : DefaultTireQuotaFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultTireQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumDefaultTireQuotaAdjuster(var minimum: Double) : DefaultTireQuotaFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultTireQuota, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DefaultTireQuotaEtaMinutesOffsetAdjuster(var weight: Double) : DefaultTireQuotaFactor {
    override fun describe(): String = "plus eta minutes times $weight"

    override fun adjust(item: DefaultTireQuota, value: Double): Double = value + item.etaMinutes.toDouble() * weight
}

/** Runs the default tire quota steps in order and keeps a trace of each. */
data class DefaultTireQuotaEngine(
    var steps: MutableList<DefaultTireQuotaFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DefaultTireQuota): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: DefaultTireQuotaFactor = steps[index]
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

    fun evaluateAll(items: MutableList<DefaultTireQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDefaultTireQuotaEngine(): DefaultTireQuotaEngine {
    return DefaultTireQuotaEngine(
        steps = mutableListOf<DefaultTireQuotaFactor>(
            PercentDefaultTireQuotaAdjuster(-8.5),
            MinimumDefaultTireQuotaAdjuster(25.6),
            DefaultTireQuotaEtaMinutesOffsetAdjuster(0.63),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 94919.7,
    )
}
