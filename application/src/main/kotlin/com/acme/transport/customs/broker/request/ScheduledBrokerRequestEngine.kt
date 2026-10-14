package com.acme.transport.customs.broker.request

import com.acme.transport.kit.flags.Registry

/** One step of the scheduled broker request calculation. */
interface ScheduledBrokerRequestFactor {
    fun describe(): String
    fun adjust(item: ScheduledBrokerRequest, value: Double): Double
}

data class NameScheduledBrokerRequestAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledBrokerRequestFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: ScheduledBrokerRequest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedScheduledBrokerRequestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledBrokerRequestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledBrokerRequest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredScheduledBrokerRequestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledBrokerRequestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledBrokerRequest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapScheduledBrokerRequestAdjuster(var cap: Double) : ScheduledBrokerRequestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledBrokerRequest, value: Double): Double = minOf(value, cap)
}

data class ScheduledBrokerRequestDistanceKmOffsetAdjuster(var weight: Double) : ScheduledBrokerRequestFactor {
    override fun describe(): String = "plus distance km times $weight"

    override fun adjust(item: ScheduledBrokerRequest, value: Double): Double = value + item.distanceKm * weight
}

data class MinimumScheduledBrokerRequestAdjuster(var minimum: Double) : ScheduledBrokerRequestFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ScheduledBrokerRequest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the scheduled broker request steps in order and keeps a trace of each. */
data class ScheduledBrokerRequestEngine(
    var steps: MutableList<ScheduledBrokerRequestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ScheduledBrokerRequest): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledBrokerRequestFactor = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledBrokerRequest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultScheduledBrokerRequestEngine(flags: Registry): ScheduledBrokerRequestEngine {
    return ScheduledBrokerRequestEngine(
        steps = mutableListOf<ScheduledBrokerRequestFactor>(
            NameScheduledBrokerRequestAdjuster(
                mutableMapOf<String, Double>("central" to 0.81, "retail" to 1.27),
            ),
            FlaggedScheduledBrokerRequestAdjuster(flags, "scheduled-broker-request-lenient", 4.2),
            TieredScheduledBrokerRequestAdjuster(
                mutableListOf<Double>(379.0, 1638.0, 9455.0),
                mutableListOf<Double>(0.85, 1.04, 0.93),
            ),
            CapScheduledBrokerRequestAdjuster(39057.0),
            ScheduledBrokerRequestDistanceKmOffsetAdjuster(0.64),
            MinimumScheduledBrokerRequestAdjuster(15.6),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 78488.2,
    )
}
