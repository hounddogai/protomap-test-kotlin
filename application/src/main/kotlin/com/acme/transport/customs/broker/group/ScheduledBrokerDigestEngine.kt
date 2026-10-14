package com.acme.transport.customs.broker.group

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled broker digest calculation. */
interface ScheduledBrokerDigestStep {
    fun describe(): String
    fun adjust(item: ScheduledBrokerDigest, value: Double): Double
}

data class TieredScheduledBrokerDigestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledBrokerDigestStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledBrokerDigest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedScheduledBrokerDigestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledBrokerDigestStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledBrokerDigest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelScheduledBrokerDigestAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledBrokerDigestStep {
    override fun describe(): String = "by label"

    override fun adjust(item: ScheduledBrokerDigest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapScheduledBrokerDigestAdjuster(var cap: Double) : ScheduledBrokerDigestStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledBrokerDigest, value: Double): Double = minOf(value, cap)
}

/** Runs the scheduled broker digest steps in order and keeps a trace of each. */
data class ScheduledBrokerDigestEngine(
    var steps: MutableList<ScheduledBrokerDigestStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledBrokerDigest): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledBrokerDigest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledBrokerDigestEngine(flags: Registry): ScheduledBrokerDigestEngine {
    return ScheduledBrokerDigestEngine(
        steps = mutableListOf<ScheduledBrokerDigestStep>(
            TieredScheduledBrokerDigestAdjuster(
                mutableListOf<Double>(2820.0, 6431.0, 7139.0),
                mutableListOf<Double>(1.03, 1.04, 1.03),
            ),
            FlaggedScheduledBrokerDigestAdjuster(flags, "scheduled-broker-digest-legacy", 8.7),
            LabelScheduledBrokerDigestAdjuster(
                mutableMapOf<String, Double>("online" to 1.18, "retail" to 0.74, "east" to 0.8),
            ),
            CapScheduledBrokerDigestAdjuster(8942.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 45424.4,
    )
}
