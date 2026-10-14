package com.acme.transport.network.servicearea.score

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending service area bundle calculation. */
interface PendingServiceAreaBundleAdjuster {
    fun describe(): String
    fun adjust(item: PendingServiceAreaBundle, value: Double): Double
}

data class PendingServiceAreaBundleMarginOffsetAdjuster(var weight: Double) : PendingServiceAreaBundleAdjuster {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: PendingServiceAreaBundle, value: Double): Double = value + item.margin * weight
}

data class CapPendingServiceAreaBundleAdjuster(var cap: Double) : PendingServiceAreaBundleAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingServiceAreaBundle, value: Double): Double = minOf(value, cap)
}

data class PercentPendingServiceAreaBundleAdjuster(var percent: Double) : PendingServiceAreaBundleAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: PendingServiceAreaBundle, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class FlaggedPendingServiceAreaBundleAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingServiceAreaBundleAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingServiceAreaBundle, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the pending service area bundle steps in order and keeps a trace of each. */
data class PendingServiceAreaBundleEngine(
    var steps: MutableList<PendingServiceAreaBundleAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: PendingServiceAreaBundle): Double {
        var current = item.weightedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<PendingServiceAreaBundle>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultPendingServiceAreaBundleEngine(flags: Registry): PendingServiceAreaBundleEngine {
    return PendingServiceAreaBundleEngine(
        steps = mutableListOf<PendingServiceAreaBundleAdjuster>(
            PendingServiceAreaBundleMarginOffsetAdjuster(0.65),
            CapPendingServiceAreaBundleAdjuster(27762.0),
            PercentPendingServiceAreaBundleAdjuster(22.4),
            FlaggedPendingServiceAreaBundleAdjuster(flags, "pending-service-area-bundle-weighted", 0.5),
        ),
        trace = mutableListOf<String>(),
        floor = 1.4,
        ceiling = 30426.2,
    )
}
