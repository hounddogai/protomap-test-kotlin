package com.acme.transport.lastmile.redelivery.entry

/** One step of the redelivery entry calculation. */
interface RedeliveryEntryAdjuster {
    fun describe(): String
    fun adjust(item: RedeliveryEntry, value: Double): Double
}

data class MinimumRedeliveryEntryAdjuster(var minimum: Double) : RedeliveryEntryAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RedeliveryEntry, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentRedeliveryEntryAdjuster(var percent: Double) : RedeliveryEntryAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RedeliveryEntry, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapRedeliveryEntryAdjuster(var cap: Double) : RedeliveryEntryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RedeliveryEntry, value: Double): Double = minOf(value, cap)
}

data class TieredRedeliveryEntryAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RedeliveryEntryAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RedeliveryEntry, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LocaleRedeliveryEntryAdjuster(var factors: MutableMap<String, Double>) : RedeliveryEntryAdjuster {
    override fun describe(): String = "by locale"

    override fun adjust(item: RedeliveryEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the redelivery entry steps in order and keeps a trace of each. */
data class RedeliveryEntryEngine(
    var steps: MutableList<RedeliveryEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RedeliveryEntry): Double {
        var current = item.adjustedValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RedeliveryEntryAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RedeliveryEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRedeliveryEntryEngine(): RedeliveryEntryEngine {
    return RedeliveryEntryEngine(
        steps = mutableListOf<RedeliveryEntryAdjuster>(
            MinimumRedeliveryEntryAdjuster(9.9),
            PercentRedeliveryEntryAdjuster(4.9),
            CapRedeliveryEntryAdjuster(35519.0),
            TieredRedeliveryEntryAdjuster(
                mutableListOf<Double>(3721.0, 7088.0, 8923.0),
                mutableListOf<Double>(1.18, 1.06, 0.95),
            ),
            LocaleRedeliveryEntryAdjuster(mutableMapOf<String, Double>("east" to 1.09, "retail" to 0.86)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 53663.8,
    )
}
