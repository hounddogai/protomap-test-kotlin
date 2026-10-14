package com.acme.transport.network.hub.overrideitem

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the hub override calculation. */
interface HubOverrideFactor {
    fun describe(): String
    fun adjust(item: HubOverride, value: Double): Double
}

data class FlaggedHubOverrideAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HubOverrideFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HubOverride, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LaneHubOverrideAdjuster(var factors: MutableMap<String, Double>) : HubOverrideFactor {
    override fun describe(): String = "by lane"

    override fun adjust(item: HubOverride, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class HubOverrideExpiresAtOffsetAdjuster(var weight: Double) : HubOverrideFactor {
    override fun describe(): String = "plus expires at times $weight"

    override fun adjust(item: HubOverride, value: Double): Double = value + item.expiresAt.toDouble() * weight
}

data class RoundingHubOverrideAdjuster(var step: Double) : HubOverrideFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HubOverride, value: Double): Double = roundTo(value, step)
}

data class PercentHubOverrideAdjuster(var percent: Double) : HubOverrideFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HubOverride, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class CapHubOverrideAdjuster(var cap: Double) : HubOverrideFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HubOverride, value: Double): Double = minOf(value, cap)
}

/** Runs the hub override steps in order and keeps a trace of each. */
data class HubOverrideEngine(
    var steps: MutableList<HubOverrideFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: HubOverride): Double {
        var current = item.netVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<HubOverride>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultHubOverrideEngine(flags: Registry): HubOverrideEngine {
    return HubOverrideEngine(
        steps = mutableListOf<HubOverrideFactor>(
            FlaggedHubOverrideAdjuster(flags, "hub-override-lenient", -9.9),
            LaneHubOverrideAdjuster(mutableMapOf<String, Double>("online" to 1.07, "south" to 0.7)),
            HubOverrideExpiresAtOffsetAdjuster(1.25),
            RoundingHubOverrideAdjuster(1.0),
            PercentHubOverrideAdjuster(19.8),
            CapHubOverrideAdjuster(28730.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 68423.9,
    )
}
