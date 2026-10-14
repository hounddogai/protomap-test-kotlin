package com.acme.transport.network.hub.hubentry

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the hub bundle calculation. */
interface HubBundleFactor {
    fun describe(): String
    fun adjust(item: HubBundle, value: Double): Double
}

data class FlaggedHubBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HubBundleFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HubBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapHubBundleAdjuster(var cap: Double) : HubBundleFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: HubBundle, value: Double): Double = minOf(value, cap)
}

data class TieredHubBundleAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HubBundleFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HubBundle, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the hub bundle steps in order and keeps a trace of each. */
data class HubBundleEngine(
    var steps: MutableList<HubBundleFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: HubBundle): Double {
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

    fun evaluateAll(items: MutableList<HubBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultHubBundleEngine(flags: Registry): HubBundleEngine {
    return HubBundleEngine(
        steps = mutableListOf<HubBundleFactor>(
            FlaggedHubBundleAdjuster(flags, "hub-bundle-batched", 2.3),
            CapHubBundleAdjuster(22573.0),
            TieredHubBundleAdjuster(
                mutableListOf<Double>(5274.0, 8649.0, 8764.0),
                mutableListOf<Double>(1.14, 1.05, 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 4709.0,
    )
}
