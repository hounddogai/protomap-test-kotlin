package com.acme.transport.emissions.emissionfactor.assignment

/** One step of the emission factor audit calculation. */
interface EmissionFactorAuditAdjuster {
    fun describe(): String
    fun adjust(item: EmissionFactorAudit, value: Double): Double
}

data class TieredEmissionFactorAuditAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : EmissionFactorAuditAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: EmissionFactorAudit, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumEmissionFactorAuditAdjuster(var minimum: Double) : EmissionFactorAuditAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: EmissionFactorAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class EmissionFactorAuditStartsAtOffsetAdjuster(var weight: Double) : EmissionFactorAuditAdjuster {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: EmissionFactorAudit, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class LocaleEmissionFactorAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : EmissionFactorAuditAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: EmissionFactorAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the emission factor audit steps in order and keeps a trace of each. */
data class EmissionFactorAuditEngine(
    var steps: MutableList<EmissionFactorAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: EmissionFactorAudit): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: EmissionFactorAuditAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<EmissionFactorAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultEmissionFactorAuditEngine(): EmissionFactorAuditEngine {
    return EmissionFactorAuditEngine(
        steps = mutableListOf<EmissionFactorAuditAdjuster>(
            TieredEmissionFactorAuditAdjuster(
                mutableListOf<Double>(2019.0, 5303.0, 9704.0, 9860.0),
                mutableListOf<Double>(1.05, 0.91, 0.96, 0.85),
            ),
            MinimumEmissionFactorAuditAdjuster(9.9),
            EmissionFactorAuditStartsAtOffsetAdjuster(1.43),
            LocaleEmissionFactorAuditAdjuster(
                mutableMapOf<String, Double>("central" to 0.83, "north" to 0.79, "online" to 1.05),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.0,
        ceiling = 93602.0,
    )
}
