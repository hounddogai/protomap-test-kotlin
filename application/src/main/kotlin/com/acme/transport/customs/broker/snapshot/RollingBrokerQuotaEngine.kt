package com.acme.transport.customs.broker.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the rolling broker quota calculation. */
interface RollingBrokerQuotaAdjuster {
    fun describe(): String
    fun adjust(item: RollingBrokerQuota, value: Double): Double
}

data class RoundingRollingBrokerQuotaAdjuster(var step: Double) : RollingBrokerQuotaAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingBrokerQuota, value: Double): Double = roundTo(value, step)
}

data class PercentRollingBrokerQuotaAdjuster(var percent: Double) : RollingBrokerQuotaAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingBrokerQuota, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedRollingBrokerQuotaAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingBrokerQuotaAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingBrokerQuota, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredRollingBrokerQuotaAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RollingBrokerQuotaAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RollingBrokerQuota, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelRollingBrokerQuotaAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingBrokerQuotaAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: RollingBrokerQuota, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RollingBrokerQuotaPriorityOffsetAdjuster(var weight: Double) : RollingBrokerQuotaAdjuster {
    override fun describe(): String = "plus priority times $weight"

    override fun adjust(item: RollingBrokerQuota, value: Double): Double = value + item.priority.toDouble() * weight
}

/** Runs the rolling broker quota steps in order and keeps a trace of each. */
data class RollingBrokerQuotaEngine(
    var steps: MutableList<RollingBrokerQuotaAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingBrokerQuota): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingBrokerQuotaAdjuster = steps[index]
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

    fun evaluateAll(items: MutableList<RollingBrokerQuota>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingBrokerQuotaEngine(flags: Registry): RollingBrokerQuotaEngine {
    return RollingBrokerQuotaEngine(
        steps = mutableListOf<RollingBrokerQuotaAdjuster>(
            RoundingRollingBrokerQuotaAdjuster(1.0),
            PercentRollingBrokerQuotaAdjuster(-10.8),
            FlaggedRollingBrokerQuotaAdjuster(flags, "rolling-broker-quota-capped", 3.2),
            TieredRollingBrokerQuotaAdjuster(
                mutableListOf<Double>(1886.0, 7638.0),
                mutableListOf<Double>(0.81, 0.88),
            ),
            LabelRollingBrokerQuotaAdjuster(
                mutableMapOf<String, Double>("central" to 1.09, "retail" to 1.23, "east" to 0.89),
            ),
            RollingBrokerQuotaPriorityOffsetAdjuster(0.89),
        ),
        trace = mutableListOf<String>(),
        floor = 4.8,
        ceiling = 77039.6,
    )
}
