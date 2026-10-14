package com.acme.transport.customs.broker.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the capped broker slot calculation. */
interface CappedBrokerSlotModifier {
    fun describe(): String
    fun adjust(item: CappedBrokerSlot, value: Double): Double
}

data class RoundingCappedBrokerSlotAdjuster(var step: Double) : CappedBrokerSlotModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: CappedBrokerSlot, value: Double): Double = roundTo(value, step)
}

data class DepotCappedBrokerSlotAdjuster(var factors: MutableMap<String, Double>) : CappedBrokerSlotModifier {
    override fun describe(): String = "by depot"

    override fun adjust(item: CappedBrokerSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCappedBrokerSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedBrokerSlotModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedBrokerSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapCappedBrokerSlotAdjuster(var cap: Double) : CappedBrokerSlotModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedBrokerSlot, value: Double): Double = minOf(value, cap)
}

data class MinimumCappedBrokerSlotAdjuster(var minimum: Double) : CappedBrokerSlotModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedBrokerSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class PercentCappedBrokerSlotAdjuster(var percent: Double) : CappedBrokerSlotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedBrokerSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the capped broker slot steps in order and keeps a trace of each. */
data class CappedBrokerSlotEngine(
    var steps: MutableList<CappedBrokerSlotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: CappedBrokerSlot): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: CappedBrokerSlotModifier = steps[index]
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

    fun evaluateAll(items: MutableList<CappedBrokerSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultCappedBrokerSlotEngine(flags: Registry): CappedBrokerSlotEngine {
    return CappedBrokerSlotEngine(
        steps = mutableListOf<CappedBrokerSlotModifier>(
            RoundingCappedBrokerSlotAdjuster(5.0),
            DepotCappedBrokerSlotAdjuster(mutableMapOf<String, Double>("retail" to 0.76)),
            FlaggedCappedBrokerSlotAdjuster(flags, "capped-broker-slot-regional", -6.5),
            CapCappedBrokerSlotAdjuster(47738.0),
            MinimumCappedBrokerSlotAdjuster(42.0),
            PercentCappedBrokerSlotAdjuster(11.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.6,
        ceiling = 40955.2,
    )
}
