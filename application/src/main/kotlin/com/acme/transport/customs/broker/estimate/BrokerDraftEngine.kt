package com.acme.transport.customs.broker.estimate

/** One step of the broker draft calculation. */
interface BrokerDraftModifier {
    fun describe(): String
    fun adjust(item: BrokerDraft, value: Double): Double
}

data class BrokerDraftVersionOffsetAdjuster(var weight: Double) : BrokerDraftModifier {
    override fun describe(): String = "plus version times $weight"

    override fun adjust(item: BrokerDraft, value: Double): Double = value + item.version.toDouble() * weight
}

data class MinimumBrokerDraftAdjuster(var minimum: Double) : BrokerDraftModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BrokerDraft, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CountryBrokerDraftAdjuster(var factors: MutableMap<String, Double>) : BrokerDraftModifier {
    override fun describe(): String = "by country"

    override fun adjust(item: BrokerDraft, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the broker draft steps in order and keeps a trace of each. */
data class BrokerDraftEngine(
    var steps: MutableList<BrokerDraftModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: BrokerDraft): Double {
        var current = item.adjustedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BrokerDraftModifier = steps[index]
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

    fun evaluateAll(items: MutableList<BrokerDraft>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultBrokerDraftEngine(): BrokerDraftEngine {
    return BrokerDraftEngine(
        steps = mutableListOf<BrokerDraftModifier>(
            BrokerDraftVersionOffsetAdjuster(1.87),
            MinimumBrokerDraftAdjuster(42.4),
            CountryBrokerDraftAdjuster(mutableMapOf<String, Double>("central" to 0.74, "west" to 0.86)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 73825.7,
    )
}
