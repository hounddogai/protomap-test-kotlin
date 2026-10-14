package com.acme.transport.linehaul.relay.relaypolicy

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the pending relay threshold calculation. */
interface PendingRelayThresholdModifier {
    fun describe(): String
    fun adjust(item: PendingRelayThreshold, value: Double): Double
}

data class FlaggedPendingRelayThresholdAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : PendingRelayThresholdModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: PendingRelayThreshold, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredPendingRelayThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : PendingRelayThresholdModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: PendingRelayThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapPendingRelayThresholdAdjuster(var cap: Double) : PendingRelayThresholdModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: PendingRelayThreshold, value: Double): Double = minOf(value, cap)
}

/** Runs the pending relay threshold steps in order and keeps a trace of each. */
data class PendingRelayThresholdEngine(
    var steps: MutableList<PendingRelayThresholdModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: PendingRelayThreshold): Double {
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

    fun evaluateAll(items: MutableList<PendingRelayThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultPendingRelayThresholdEngine(flags: Registry): PendingRelayThresholdEngine {
    return PendingRelayThresholdEngine(
        steps = mutableListOf<PendingRelayThresholdModifier>(
            FlaggedPendingRelayThresholdAdjuster(flags, "pending-relay-threshold-default", 5.3),
            TieredPendingRelayThresholdAdjuster(
                mutableListOf<Double>(802.0, 2202.0, 7623.0, 9370.0),
                mutableListOf<Double>(1.07, 1.18, 1.11, 0.86),
            ),
            CapPendingRelayThresholdAdjuster(19181.0),
        ),
        trace = mutableListOf<String>(),
        floor = 1.1,
        ceiling = 86367.8,
    )
}
