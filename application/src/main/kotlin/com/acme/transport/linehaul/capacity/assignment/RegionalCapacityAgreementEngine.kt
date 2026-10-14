package com.acme.transport.linehaul.capacity.assignment

/** One step of the regional capacity agreement calculation. */
interface RegionalCapacityAgreementAdjuster {
    fun describe(): String
    fun adjust(item: RegionalCapacityAgreement, value: Double): Double
}

data class MinimumRegionalCapacityAgreementAdjuster(var minimum: Double) : RegionalCapacityAgreementAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RegionalCapacityAgreement, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelRegionalCapacityAgreementAdjuster(
    var factors: MutableMap<String, Double>,
) : RegionalCapacityAgreementAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: RegionalCapacityAgreement, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentRegionalCapacityAgreementAdjuster(var percent: Double) : RegionalCapacityAgreementAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RegionalCapacityAgreement, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the regional capacity agreement steps in order and keeps a trace of each. */
data class RegionalCapacityAgreementEngine(
    var steps: MutableList<RegionalCapacityAgreementAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RegionalCapacityAgreement): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RegionalCapacityAgreementAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RegionalCapacityAgreement>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRegionalCapacityAgreementEngine(): RegionalCapacityAgreementEngine {
    return RegionalCapacityAgreementEngine(
        steps = mutableListOf<RegionalCapacityAgreementAdjuster>(
            MinimumRegionalCapacityAgreementAdjuster(2.3),
            LabelRegionalCapacityAgreementAdjuster(mutableMapOf<String, Double>("west" to 1.23)),
            PercentRegionalCapacityAgreementAdjuster(11.1),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 52292.2,
    )
}
