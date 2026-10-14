package com.acme.transport.customs.broker.threshold

import com.acme.transport.kit.numbers.roundTo

/** One step of the pending broker draft calculation. */
interface PendingBrokerDraftModifier {
    fun describe(): String
    fun adjust(item: PendingBrokerDraft, value: Double): Double
}

data class LabelPendingBrokerDraftAdjuster(
    var factors: MutableMap<String, Double>,
) : PendingBrokerDraftModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: PendingBrokerDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumPendingBrokerDraftAdjuster(var minimum: Double) : PendingBrokerDraftModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: PendingBrokerDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredPendingBrokerDraftAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingBrokerDraftModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingBrokerDraft, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingPendingBrokerDraftAdjuster(var step: Double) : PendingBrokerDraftModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: PendingBrokerDraft, value: Double): Double = roundTo(value, step)
}

/** Runs the pending broker draft steps in order and keeps a trace of each. */
data class PendingBrokerDraftEngine(
    var steps: MutableList<PendingBrokerDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingBrokerDraft): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: PendingBrokerDraftModifier = steps[index]
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

    fun evaluateAll(items: MutableList<PendingBrokerDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingBrokerDraftEngine(): PendingBrokerDraftEngine {
    return PendingBrokerDraftEngine(
        steps = mutableListOf<PendingBrokerDraftModifier>(
            LabelPendingBrokerDraftAdjuster(mutableMapOf<String, Double>("central" to 1.26, "north" to 1.02)),
            MinimumPendingBrokerDraftAdjuster(40.7),
            TieredPendingBrokerDraftAdjuster(
                mutableListOf<Double>(1971.0, 8793.0),
                mutableListOf<Double>(0.83, 1.12),
            ),
            RoundingPendingBrokerDraftAdjuster(5.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 27693.4,
    )
}
