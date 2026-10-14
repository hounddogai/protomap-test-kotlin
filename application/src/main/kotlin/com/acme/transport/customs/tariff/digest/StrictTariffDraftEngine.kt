package com.acme.transport.customs.tariff.digest

/** One step of the strict tariff draft calculation. */
interface StrictTariffDraftFactor {
    fun describe(): String
    fun adjust(item: StrictTariffDraft, value: Double): Double
}

data class PercentStrictTariffDraftAdjuster(var percent: Double) : StrictTariffDraftFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: StrictTariffDraft, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RegionStrictTariffDraftAdjuster(var factors: MutableMap<String, Double>) : StrictTariffDraftFactor {
    override fun describe(): String = "by region"

    override fun adjust(item: StrictTariffDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapStrictTariffDraftAdjuster(var cap: Double) : StrictTariffDraftFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: StrictTariffDraft, value: Double): Double = minOf(value, cap)
}

/** Runs the strict tariff draft steps in order and keeps a trace of each. */
data class StrictTariffDraftEngine(
    var steps: MutableList<StrictTariffDraftFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: StrictTariffDraft): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: StrictTariffDraftFactor = steps[index]
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

    fun evaluateAll(items: MutableList<StrictTariffDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultStrictTariffDraftEngine(): StrictTariffDraftEngine {
    return StrictTariffDraftEngine(
        steps = mutableListOf<StrictTariffDraftFactor>(
            PercentStrictTariffDraftAdjuster(15.8),
            RegionStrictTariffDraftAdjuster(
                mutableMapOf<String, Double>("west" to 1.09, "north" to 1.16, "central" to 1.03),
            ),
            CapStrictTariffDraftAdjuster(17439.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 94913.9,
    )
}
