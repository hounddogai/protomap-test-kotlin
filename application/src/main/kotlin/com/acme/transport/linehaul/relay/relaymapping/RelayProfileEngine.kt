package com.acme.transport.linehaul.relay.relaymapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the relay profile calculation. */
interface RelayProfileAdjuster {
    fun describe(): String
    fun adjust(item: RelayProfile, value: Double): Double
}

data class FlaggedRelayProfileAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RelayProfileAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RelayProfile, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapRelayProfileAdjuster(var cap: Double) : RelayProfileAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RelayProfile, value: Double): Double = minOf(value, cap)
}

data class TitleRelayProfileAdjuster(var factors: MutableMap<String, Double>) : RelayProfileAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: RelayProfile, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumRelayProfileAdjuster(var minimum: Double) : RelayProfileAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RelayProfile, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the relay profile steps in order and keeps a trace of each. */
data class RelayProfileEngine(
    var steps: MutableList<RelayProfileAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: RelayProfile): Double {
        var current = item.weightedTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RelayProfile>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultRelayProfileEngine(flags: Registry): RelayProfileEngine {
    return RelayProfileEngine(
        steps = mutableListOf<RelayProfileAdjuster>(
            FlaggedRelayProfileAdjuster(flags, "relay-profile-legacy", -9.0),
            CapRelayProfileAdjuster(17672.0),
            TitleRelayProfileAdjuster(mutableMapOf<String, Double>("north" to 0.93)),
            MinimumRelayProfileAdjuster(29.5),
        ),
        trace = mutableListOf<String>(),
        floor = 4.5,
        ceiling = 73228.2,
    )
}
