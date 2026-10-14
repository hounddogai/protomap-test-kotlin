package com.acme.transport.fleetops.leasereturn.draft

/** One step of the adaptive lease return draft calculation. */
interface AdaptiveLeaseReturnDraftModifier {
    fun describe(): String
    fun adjust(item: AdaptiveLeaseReturnDraft, value: Double): Double
}

data class CapAdaptiveLeaseReturnDraftAdjuster(var cap: Double) : AdaptiveLeaseReturnDraftModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: AdaptiveLeaseReturnDraft, value: Double): Double = minOf(value, cap)
}

data class TieredAdaptiveLeaseReturnDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : AdaptiveLeaseReturnDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: AdaptiveLeaseReturnDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class AdaptiveLeaseReturnDraftOdometerOffsetAdjuster(
    var weight: Double,
) : AdaptiveLeaseReturnDraftModifier {
    override fun describe(): String = "plus odometer times $weight"

    override fun adjust(item: AdaptiveLeaseReturnDraft, value: Double): Double {
        return value + item.odometer.toDouble() * weight
    }
}

data class MinimumAdaptiveLeaseReturnDraftAdjuster(var minimum: Double) : AdaptiveLeaseReturnDraftModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: AdaptiveLeaseReturnDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SourceAdaptiveLeaseReturnDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : AdaptiveLeaseReturnDraftModifier {
    override fun describe(): String = "by source"

    override fun adjust(item: AdaptiveLeaseReturnDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the adaptive lease return draft steps in order and keeps a trace of each. */
data class AdaptiveLeaseReturnDraftEngine(
    var steps: MutableList<AdaptiveLeaseReturnDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: AdaptiveLeaseReturnDraft): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: AdaptiveLeaseReturnDraftModifier = steps[index]
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

    fun evaluateAll(items: MutableList<AdaptiveLeaseReturnDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultAdaptiveLeaseReturnDraftEngine(): AdaptiveLeaseReturnDraftEngine {
    return AdaptiveLeaseReturnDraftEngine(
        steps = mutableListOf<AdaptiveLeaseReturnDraftModifier>(
            CapAdaptiveLeaseReturnDraftAdjuster(8048.0),
            TieredAdaptiveLeaseReturnDraftAdjuster(
                mutableListOf<Double>(1696.0, 8172.0),
                mutableListOf<Double>(1.04, 0.82),
            ),
            AdaptiveLeaseReturnDraftOdometerOffsetAdjuster(1.65),
            MinimumAdaptiveLeaseReturnDraftAdjuster(42.5),
            SourceAdaptiveLeaseReturnDraftAdjuster(
                mutableMapOf<String, Double>("central" to 1.34, "north" to 0.99),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 36107.2,
    )
}
