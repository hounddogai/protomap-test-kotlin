package com.acme.transport.customs.broker.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the broker summary calculation. */
interface BrokerSummaryAdjuster {
    fun describe(): String
    fun adjust(item: BrokerSummary, value: Double): Double
}

data class CountryBrokerSummaryAdjuster(var factors: MutableMap<String, Double>) : BrokerSummaryAdjuster {
    override fun describe(): String = "by country"

    override fun adjust(item: BrokerSummary, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class BrokerSummaryBudgetOffsetAdjuster(var weight: Double) : BrokerSummaryAdjuster {
    override fun describe(): String = "plus budget times $weight"

    override fun adjust(item: BrokerSummary, value: Double): Double = value + item.budget * weight
}

data class FlaggedBrokerSummaryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BrokerSummaryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BrokerSummary, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapBrokerSummaryAdjuster(var cap: Double) : BrokerSummaryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BrokerSummary, value: Double): Double = minOf(value, cap)
}

/** Runs the broker summary steps in order and keeps a trace of each. */
data class BrokerSummaryEngine(
    var steps: MutableList<BrokerSummaryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BrokerSummary): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BrokerSummary>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBrokerSummaryEngine(flags: Registry): BrokerSummaryEngine {
    return BrokerSummaryEngine(
        steps = mutableListOf<BrokerSummaryAdjuster>(
            CountryBrokerSummaryAdjuster(
                mutableMapOf<String, Double>("west" to 1.27, "central" to 0.85, "online" to 0.8),
            ),
            BrokerSummaryBudgetOffsetAdjuster(1.65),
            FlaggedBrokerSummaryAdjuster(flags, "broker-summary-scheduled", 5.2),
            CapBrokerSummaryAdjuster(44143.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.3,
        ceiling = 18864.7,
    )
}
