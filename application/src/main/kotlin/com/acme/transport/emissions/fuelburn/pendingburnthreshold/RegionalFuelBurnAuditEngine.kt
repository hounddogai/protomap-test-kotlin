package com.acme.transport.emissions.fuelburn.pendingburnthreshold

/** One step of the regional fuel burn audit calculation. */
interface RegionalFuelBurnAuditStep {
    fun describe(): String
    fun adjust(item: RegionalFuelBurnAudit, value: Double): Double
}

data class LabelRegionalFuelBurnAuditAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalFuelBurnAuditStep {
    override fun describe(): String = "by label"

    override fun adjust(item: RegionalFuelBurnAudit, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RegionalFuelBurnAuditDiscountOffsetAdjuster(var weight: Double) : RegionalFuelBurnAuditStep {
    override fun describe(): String = "plus discount times $weight"

    override fun adjust(item: RegionalFuelBurnAudit, value: Double): Double = value + item.discount * weight
}

data class PercentRegionalFuelBurnAuditAdjuster(var percent: Double) : RegionalFuelBurnAuditStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalFuelBurnAudit, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional fuel burn audit steps in order and keeps a trace of each. */
data class RegionalFuelBurnAuditEngine(
    var steps: MutableList<RegionalFuelBurnAuditStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RegionalFuelBurnAudit): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalFuelBurnAuditStep = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalFuelBurnAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRegionalFuelBurnAuditEngine(): RegionalFuelBurnAuditEngine {
    return RegionalFuelBurnAuditEngine(
        steps = mutableListOf<RegionalFuelBurnAuditStep>(
            LabelRegionalFuelBurnAuditAdjuster(mutableMapOf<String, Double>("central" to 1.2, "south" to 1.15)),
            RegionalFuelBurnAuditDiscountOffsetAdjuster(0.27),
            PercentRegionalFuelBurnAuditAdjuster(20.0),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 83809.2,
    )
}
