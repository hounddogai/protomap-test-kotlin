package com.acme.transport.network.hub.snapshot

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the hub snapshot calculation. */
interface HubSnapshotModifier {
    fun describe(): String
    fun adjust(item: HubSnapshot, value: Double): Double
}

data class PercentHubSnapshotAdjuster(var percent: Double) : HubSnapshotModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: HubSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredHubSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HubSnapshotModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HubSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LaneHubSnapshotAdjuster(var factors: MutableMap<String, Double>) : HubSnapshotModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: HubSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the hub snapshot steps in order and keeps a trace of each. */
data class HubSnapshotEngine(
    var steps: MutableList<HubSnapshotModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: HubSnapshot): Double {
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

    fun evaluateAll(items: MutableList<HubSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultHubSnapshotEngine(): HubSnapshotEngine {
    return HubSnapshotEngine(
        steps = mutableListOf<HubSnapshotModifier>(
            PercentHubSnapshotAdjuster(-11.8),
            TieredHubSnapshotAdjuster(
                mutableListOf<Double>(5676.0, 8763.0, 9887.0),
                mutableListOf<Double>(1.05, 0.98, 0.93),
            ),
            LaneHubSnapshotAdjuster(mutableMapOf<String, Double>("north" to 1.31, "online" to 1.18)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.8,
        ceiling = 54770.6,
    )
}
