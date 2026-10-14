package com.acme.transport.customs.broker.exception

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the broker exception calculation. */
interface BrokerExceptionAdjuster {
    fun describe(): String
    fun adjust(item: BrokerException, value: Double): Double
}

data class PercentBrokerExceptionAdjuster(var percent: Double) : BrokerExceptionAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: BrokerException, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumBrokerExceptionAdjuster(var minimum: Double) : BrokerExceptionAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BrokerException, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class SegmentBrokerExceptionAdjuster(var factors: MutableMap<String, Double>) : BrokerExceptionAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: BrokerException, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedBrokerExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BrokerExceptionAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BrokerException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class BrokerExceptionPriorityOffsetAdjuster(var weight: Double) : BrokerExceptionAdjuster {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: BrokerException, value: Double): Double = value + item.priority.toDouble() * weight
}

/** Runs the broker exception steps in order and keeps a trace of each. */
data class BrokerExceptionEngine(
    var steps: MutableList<BrokerExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: BrokerException): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BrokerException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultBrokerExceptionEngine(flags: Registry): BrokerExceptionEngine {
    return BrokerExceptionEngine(
        steps = mutableListOf<BrokerExceptionAdjuster>(
            PercentBrokerExceptionAdjuster(-0.6),
            MinimumBrokerExceptionAdjuster(34.7),
            SegmentBrokerExceptionAdjuster(
                mutableMapOf<String, Double>("north" to 0.9, "central" to 1.16, "east" to 1.24),
            ),
            FlaggedBrokerExceptionAdjuster(flags, "broker-exception-adaptive", 0.2),
            BrokerExceptionPriorityOffsetAdjuster(0.06),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 55477.2,
    )
}
