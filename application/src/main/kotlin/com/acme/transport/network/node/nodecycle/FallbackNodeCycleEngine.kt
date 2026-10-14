package com.acme.transport.network.node.nodecycle

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the fallback node cycle calculation. */
interface FallbackNodeCycleAdjuster {
    fun describe(): String
    fun adjust(item: FallbackNodeCycle, value: Double): Double
}

data class NameFallbackNodeCycleAdjuster(var factors: MutableMap<String, Double>) : FallbackNodeCycleAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: FallbackNodeCycle, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedFallbackNodeCycleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : FallbackNodeCycleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: FallbackNodeCycle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentFallbackNodeCycleAdjuster(var percent: Double) : FallbackNodeCycleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: FallbackNodeCycle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class MinimumFallbackNodeCycleAdjuster(var minimum: Double) : FallbackNodeCycleAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: FallbackNodeCycle, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class CapFallbackNodeCycleAdjuster(var cap: Double) : FallbackNodeCycleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: FallbackNodeCycle, value: Double): Double = minOf(value, cap)
}

/** Runs the fallback node cycle steps in order and keeps a trace of each. */
data class FallbackNodeCycleEngine(
    var steps: MutableList<FallbackNodeCycleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: FallbackNodeCycle): Double {
        var current = item.adjustedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<FallbackNodeCycle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultFallbackNodeCycleEngine(flags: Registry): FallbackNodeCycleEngine {
    return FallbackNodeCycleEngine(
        steps = mutableListOf<FallbackNodeCycleAdjuster>(
            NameFallbackNodeCycleAdjuster(mutableMapOf<String, Double>("east" to 1.04, "south" to 1.3)),
            FlaggedFallbackNodeCycleAdjuster(flags, "fallback-node-cycle-seasonal", 4.3),
            PercentFallbackNodeCycleAdjuster(-14.3),
            MinimumFallbackNodeCycleAdjuster(8.1),
            CapFallbackNodeCycleAdjuster(49136.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.9,
        ceiling = 68037.8,
    )
}
