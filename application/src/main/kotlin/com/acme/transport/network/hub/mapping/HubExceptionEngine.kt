package com.acme.transport.network.hub.mapping

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the hub exception calculation. */
interface HubExceptionAdjuster {
    fun describe(): String
    fun adjust(item: HubException, value: Double): Double
}

data class CapHubExceptionAdjuster(var cap: Double) : HubExceptionAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HubException, value: Double): Double = minOf(value, cap)
}

data class FlaggedHubExceptionAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HubExceptionAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HubException, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class HubExceptionRateOffsetAdjuster(var weight: Double) : HubExceptionAdjuster {
    override fun describe(): String = "plus rate times $weight"

    override fun adjust(item: HubException, value: Double): Double = value + item.rate * weight
}

/** Runs the hub exception steps in order and keeps a trace of each. */
data class HubExceptionEngine(
    var steps: MutableList<HubExceptionAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: HubException): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<HubException>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultHubExceptionEngine(flags: Registry): HubExceptionEngine {
    return HubExceptionEngine(
        steps = mutableListOf<HubExceptionAdjuster>(
            CapHubExceptionAdjuster(30766.0),
            FlaggedHubExceptionAdjuster(flags, "hub-exception-batched", 0.0),
            HubExceptionRateOffsetAdjuster(1.58),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 68278.1,
    )
}
