package com.acme.transport.lastmile.redelivery.redeliverytemplate

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global redelivery override calculation. */
interface GlobalRedeliveryOverrideFactor {
    fun describe(): String
    fun adjust(item: GlobalRedeliveryOverride, value: Double): Double
}

data class FlaggedGlobalRedeliveryOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalRedeliveryOverrideFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalRedeliveryOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumGlobalRedeliveryOverrideAdjuster(var minimum: Double) : GlobalRedeliveryOverrideFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: GlobalRedeliveryOverride, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DepotGlobalRedeliveryOverrideAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalRedeliveryOverrideFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: GlobalRedeliveryOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the global redelivery override steps in order and keeps a trace of each. */
data class GlobalRedeliveryOverrideEngine(
    var steps: MutableList<GlobalRedeliveryOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: GlobalRedeliveryOverride): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalRedeliveryOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultGlobalRedeliveryOverrideEngine(flags: Registry): GlobalRedeliveryOverrideEngine {
    return GlobalRedeliveryOverrideEngine(
        steps = mutableListOf<GlobalRedeliveryOverrideFactor>(
            FlaggedGlobalRedeliveryOverrideAdjuster(flags, "global-redelivery-override-pending", 1.7),
            MinimumGlobalRedeliveryOverrideAdjuster(0.2),
            DepotGlobalRedeliveryOverrideAdjuster(mutableMapOf<String, Double>("south" to 1.35, "east" to 1.11)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 45580.3,
    )
}
