package com.acme.transport.network.node.weightednodeagreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the capped node slot calculation. */
interface CappedNodeSlotAdjuster {
    fun describe(): String
    fun adjust(item: CappedNodeSlot, value: Double): Double
}

data class PercentCappedNodeSlotAdjuster(var percent: Double) : CappedNodeSlotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CappedNodeSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneCappedNodeSlotAdjuster(var factors: MutableMap<String, Double>) : CappedNodeSlotAdjuster {
    override fun describe(): String = "by lane"

    override fun adjust(item: CappedNodeSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCappedNodeSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CappedNodeSlotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CappedNodeSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCappedNodeSlotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CappedNodeSlotAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CappedNodeSlot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class MinimumCappedNodeSlotAdjuster(var minimum: Double) : CappedNodeSlotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CappedNodeSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapCappedNodeSlotAdjuster(var cap: Double) : CappedNodeSlotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: CappedNodeSlot, value: Double): Double = minOf(value, cap)
}

/** Runs the capped node slot steps in order and keeps a trace of each. */
data class CappedNodeSlotEngine(
    var steps: MutableList<CappedNodeSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: CappedNodeSlot): Double {
        var current = item.adjustedExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CappedNodeSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultCappedNodeSlotEngine(flags: Registry): CappedNodeSlotEngine {
    return CappedNodeSlotEngine(
        steps = mutableListOf<CappedNodeSlotAdjuster>(
            PercentCappedNodeSlotAdjuster(-18.8),
            LaneCappedNodeSlotAdjuster(mutableMapOf<String, Double>("online" to 0.77)),
            FlaggedCappedNodeSlotAdjuster(flags, "capped-node-slot-primary", -8.3),
            TieredCappedNodeSlotAdjuster(
                mutableListOf<Double>(2617.0, 3119.0, 7864.0, 8447.0),
                mutableListOf<Double>(1.15, 1.12, 1.15, 1.15),
            ),
            MinimumCappedNodeSlotAdjuster(27.8),
            CapCappedNodeSlotAdjuster(4641.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.0,
        ceiling = 51810.2,
    )
}
