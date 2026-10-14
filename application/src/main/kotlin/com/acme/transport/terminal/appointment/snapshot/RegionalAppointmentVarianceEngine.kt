package com.acme.transport.terminal.appointment.snapshot

/** One step of the regional appointment variance calculation. */
interface RegionalAppointmentVarianceModifier {
    fun describe(): String
    fun adjust(item: RegionalAppointmentVariance, value: Double): Double
}

data class PercentRegionalAppointmentVarianceAdjuster(
    var percent: Double,
) : RegionalAppointmentVarianceModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalAppointmentVariance, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredRegionalAppointmentVarianceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RegionalAppointmentVarianceModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RegionalAppointmentVariance, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class UnitRegionalAppointmentVarianceAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalAppointmentVarianceModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: RegionalAppointmentVariance, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the regional appointment variance steps in order and keeps a trace of each. */
data class RegionalAppointmentVarianceEngine(
    var steps: MutableList<RegionalAppointmentVarianceModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalAppointmentVariance): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalAppointmentVarianceModifier = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalAppointmentVariance>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalAppointmentVarianceEngine(): RegionalAppointmentVarianceEngine {
    return RegionalAppointmentVarianceEngine(
        steps = mutableListOf<RegionalAppointmentVarianceModifier>(
            PercentRegionalAppointmentVarianceAdjuster(21.1),
            TieredRegionalAppointmentVarianceAdjuster(
                mutableListOf<Double>(3303.0, 5957.0, 6057.0, 7564.0),
                mutableListOf<Double>(1.19, 1.09, 0.86, 1.01),
            ),
            UnitRegionalAppointmentVarianceAdjuster(mutableMapOf<String, Double>("west" to 1.18)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.4,
        ceiling = 84803.0,
    )
}
