package com.acme.transport.safety.nearmiss.slot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the near miss slot calculation. */
interface NearMissSlotAdjuster {
    fun describe(): String
    fun adjust(item: NearMissSlot, value: Double): Double
}

data class CapNearMissSlotAdjuster(var cap: Double) : NearMissSlotAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: NearMissSlot, value: Double): Double = minOf(value, cap)
}

data class PercentNearMissSlotAdjuster(var percent: Double) : NearMissSlotAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: NearMissSlot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumNearMissSlotAdjuster(var minimum: Double) : NearMissSlotAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: NearMissSlot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ChannelNearMissSlotAdjuster(var factors: MutableMap<String, Double>) : NearMissSlotAdjuster {
    override fun describe(): String = "by channel"

    override fun adjust(item: NearMissSlot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedNearMissSlotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : NearMissSlotAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: NearMissSlot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the near miss slot steps in order and keeps a trace of each. */
data class NearMissSlotEngine(
    var steps: MutableList<NearMissSlotAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: NearMissSlot): Double {
        var current = item.weightedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<NearMissSlot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultNearMissSlotEngine(flags: Registry): NearMissSlotEngine {
    return NearMissSlotEngine(
        steps = mutableListOf<NearMissSlotAdjuster>(
            CapNearMissSlotAdjuster(34699.0),
            PercentNearMissSlotAdjuster(8.2),
            MinimumNearMissSlotAdjuster(30.5),
            ChannelNearMissSlotAdjuster(mutableMapOf<String, Double>("north" to 1.26, "online" to 1.08)),
            FlaggedNearMissSlotAdjuster(flags, "near-miss-slot-capped", -7.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 91371.8,
    )
}
