package com.acme.transport.customs.broker.brokerslot

import com.acme.transport.kit.flags.Registry

/** One step of the rolling broker slot calculation. */
interface RollingBrokerSlotFactor {
    fun describe(): String
    fun adjust(item: RollingBrokerSlot, value: Double): Double
}

data class FlaggedRollingBrokerSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RollingBrokerSlotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RollingBrokerSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRollingBrokerSlotAdjuster(var cap: Double) : RollingBrokerSlotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingBrokerSlot, value: Double): Double = minOf(value, cap)
}

data class DepotRollingBrokerSlotAdjuster(var factors: MutableMap<String, Double>) : RollingBrokerSlotFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: RollingBrokerSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRollingBrokerSlotAdjuster(var minimum: Double) : RollingBrokerSlotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RollingBrokerSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the rolling broker slot steps in order and keeps a trace of each. */
data class RollingBrokerSlotEngine(
    var steps: MutableList<RollingBrokerSlotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RollingBrokerSlot): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: RollingBrokerSlotFactor = steps[index]
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

    fun evaluateAll(items: MutableList<RollingBrokerSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRollingBrokerSlotEngine(flags: Registry): RollingBrokerSlotEngine {
    return RollingBrokerSlotEngine(
        steps = mutableListOf<RollingBrokerSlotFactor>(
            FlaggedRollingBrokerSlotAdjuster(flags, "rolling-broker-slot-batched", 5.0),
            CapRollingBrokerSlotAdjuster(40213.0),
            DepotRollingBrokerSlotAdjuster(mutableMapOf<String, Double>("south" to 1.16, "west" to 1.07)),
            MinimumRollingBrokerSlotAdjuster(22.6),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 12836.6,
    )
}
