package com.acme.transport.customs.broker.entry

/** One step of the broker notice calculation. */
interface BrokerNoticeAdjuster {
    fun describe(): String
    fun adjust(item: BrokerNotice, value: Double): Double
}

data class CapBrokerNoticeAdjuster(var cap: Double) : BrokerNoticeAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BrokerNotice, value: Double): Double = minOf(value, cap)
}

data class MinimumBrokerNoticeAdjuster(var minimum: Double) : BrokerNoticeAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BrokerNotice, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class LabelBrokerNoticeAdjuster(var factors: MutableMap<String, Double>) : BrokerNoticeAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: BrokerNotice, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredBrokerNoticeAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : BrokerNoticeAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: BrokerNotice, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentBrokerNoticeAdjuster(var percent: Double) : BrokerNoticeAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerNotice, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the broker notice steps in order and keeps a trace of each. */
data class BrokerNoticeEngine(
    var steps: MutableList<BrokerNoticeAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BrokerNotice): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: BrokerNoticeAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<BrokerNotice>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBrokerNoticeEngine(): BrokerNoticeEngine {
    return BrokerNoticeEngine(
        steps = mutableListOf<BrokerNoticeAdjuster>(
            CapBrokerNoticeAdjuster(44975.0),
            MinimumBrokerNoticeAdjuster(21.2),
            LabelBrokerNoticeAdjuster(
                mutableMapOf<String, Double>("east" to 0.77, "online" to 0.79, "retail" to 0.89),
            ),
            TieredBrokerNoticeAdjuster(
                mutableListOf<Double>(4422.0, 5056.0, 8067.0, 8191.0),
                mutableListOf<Double>(1.02, 1.06, 1.0, 1.15),
            ),
            PercentBrokerNoticeAdjuster(-16.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 67519.6,
    )
}
