package com.acme.transport.freightpay.audit.auditproposal

/** One step of the audit forecast calculation. */
interface AuditForecastAdjuster {
    fun describe(): String
    fun adjust(item: AuditForecast, value: Double): Double
}

data class TieredAuditForecastAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AuditForecastAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AuditForecast, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumAuditForecastAdjuster(var minimum: Double) : AuditForecastAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AuditForecast, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class UnitAuditForecastAdjuster(var factors: MutableMap<String, Double>) : AuditForecastAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: AuditForecast, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapAuditForecastAdjuster(var cap: Double) : AuditForecastAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AuditForecast, value: Double): Double = minOf(value, cap)
}

/** Runs the audit forecast steps in order and keeps a trace of each. */
data class AuditForecastEngine(
    var steps: MutableList<AuditForecastAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: AuditForecast): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AuditForecastAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<AuditForecast>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultAuditForecastEngine(): AuditForecastEngine {
    return AuditForecastEngine(
        steps = mutableListOf<AuditForecastAdjuster>(
            TieredAuditForecastAdjuster(
                mutableListOf<Double>(2031.0, 3283.0, 5572.0, 7771.0),
                mutableListOf<Double>(0.98, 1.14, 0.81, 0.85),
            ),
            MinimumAuditForecastAdjuster(31.5),
            UnitAuditForecastAdjuster(mutableMapOf<String, Double>("south" to 1.1, "central" to 0.85)),
            CapAuditForecastAdjuster(3585.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.7,
        ceiling = 42490.5,
    )
}
