package com.acme.transport.customs.broker.schedule

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the broker entry calculation. */
interface BrokerEntryAdjuster {
    fun describe(): String
    fun adjust(item: BrokerEntry, value: Double): Double
}

data class FlaggedBrokerEntryAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BrokerEntryAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BrokerEntry, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class RoundingBrokerEntryAdjuster(var step: Double) : BrokerEntryAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerEntry, value: Double): Double = roundTo(value, step)
}

data class SourceBrokerEntryAdjuster(var factors: MutableMap<String, Double>) : BrokerEntryAdjuster {
    override fun describe(): String = "by source"

    override fun adjust(item: BrokerEntry, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapBrokerEntryAdjuster(var cap: Double) : BrokerEntryAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: BrokerEntry, value: Double): Double = minOf(value, cap)
}

/** Runs the broker entry steps in order and keeps a trace of each. */
data class BrokerEntryEngine(
    var steps: MutableList<BrokerEntryAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BrokerEntry): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BrokerEntry>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBrokerEntryEngine(flags: Registry): BrokerEntryEngine {
    return BrokerEntryEngine(
        steps = mutableListOf<BrokerEntryAdjuster>(
            FlaggedBrokerEntryAdjuster(flags, "broker-entry-pending", 1.1),
            RoundingBrokerEntryAdjuster(1.0),
            SourceBrokerEntryAdjuster(mutableMapOf<String, Double>("central" to 1.05)),
            CapBrokerEntryAdjuster(41561.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 46171.1,
    )
}
