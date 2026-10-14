package com.acme.transport.linehaul.relay.threshold

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the relay threshold calculation. */
interface RelayThresholdAdjuster {
    fun describe(): String
    fun adjust(item: RelayThreshold, value: Double): Double
}

data class TitleRelayThresholdAdjuster(var factors: MutableMap<String, Double>) : RelayThresholdAdjuster {
    override fun describe(): String = "by title"

    override fun adjust(item: RelayThreshold, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredRelayThresholdAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : RelayThresholdAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: RelayThreshold, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapRelayThresholdAdjuster(var cap: Double) : RelayThresholdAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RelayThreshold, value: Double): Double = minOf(value, cap)
}

/** Runs the relay threshold steps in order and keeps a trace of each. */
data class RelayThresholdEngine(
    var steps: MutableList<RelayThresholdAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: RelayThreshold): Double {
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

    fun evaluateAll(items: MutableList<RelayThreshold>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultRelayThresholdEngine(): RelayThresholdEngine {
    return RelayThresholdEngine(
        steps = mutableListOf<RelayThresholdAdjuster>(
            TitleRelayThresholdAdjuster(
                mutableMapOf<String, Double>("central" to 0.91, "east" to 0.94, "online" to 1.25),
            ),
            TieredRelayThresholdAdjuster(
                mutableListOf<Double>(7183.0, 8599.0),
                mutableListOf<Double>(0.93, 1.17),
            ),
            CapRelayThresholdAdjuster(22148.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 24930.3,
    )
}
