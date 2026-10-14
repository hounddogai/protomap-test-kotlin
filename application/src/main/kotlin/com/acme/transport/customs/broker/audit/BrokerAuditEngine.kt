package com.acme.transport.customs.broker.audit

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the broker audit calculation. */
interface BrokerAuditAdjuster {
    fun describe(): String
    fun adjust(item: BrokerAudit, value: Double): Double
}

data class RoundingBrokerAuditAdjuster(var step: Double) : BrokerAuditAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: BrokerAudit, value: Double): Double = roundTo(value, step)
}

data class MinimumBrokerAuditAdjuster(var minimum: Double) : BrokerAuditAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: BrokerAudit, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedBrokerAuditAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : BrokerAuditAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: BrokerAudit, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the broker audit steps in order and keeps a trace of each. */
data class BrokerAuditEngine(
    var steps: MutableList<BrokerAuditAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: BrokerAudit): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<BrokerAudit>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultBrokerAuditEngine(flags: Registry): BrokerAuditEngine {
    return BrokerAuditEngine(
        steps = mutableListOf<BrokerAuditAdjuster>(
            RoundingBrokerAuditAdjuster(0.5),
            MinimumBrokerAuditAdjuster(10.5),
            FlaggedBrokerAuditAdjuster(flags, "broker-audit-tiered", 7.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 61172.3,
    )
}
