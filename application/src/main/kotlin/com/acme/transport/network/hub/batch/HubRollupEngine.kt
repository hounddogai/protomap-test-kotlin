package com.acme.transport.network.hub.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the hub rollup calculation. */
interface HubRollupAdjuster {
    fun describe(): String
    fun adjust(item: HubRollup, value: Double): Double
}

data class RoundingHubRollupAdjuster(var step: Double) : HubRollupAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: HubRollup, value: Double): Double = roundTo(value, step)
}

data class HubRollupFloorValueOffsetAdjuster(var weight: Double) : HubRollupAdjuster {
    override fun describe(): String = "plus floor value times $weight"

    override fun adjust(item: HubRollup, value: Double): Double = value + item.floorValue * weight
}

data class SegmentHubRollupAdjuster(var factors: MutableMap<String, Double>) : HubRollupAdjuster {
    override fun describe(): String = "by segment"

    override fun adjust(item: HubRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedHubRollupAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : HubRollupAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: HubRollup, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredHubRollupAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : HubRollupAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: HubRollup, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the hub rollup steps in order and keeps a trace of each. */
data class HubRollupEngine(
    var steps: MutableList<HubRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: HubRollup): Double {
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

    fun evaluateAll(items: MutableList<HubRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultHubRollupEngine(flags: Registry): HubRollupEngine {
    return HubRollupEngine(
        steps = mutableListOf<HubRollupAdjuster>(
            RoundingHubRollupAdjuster(1.0),
            HubRollupFloorValueOffsetAdjuster(1.31),
            SegmentHubRollupAdjuster(mutableMapOf<String, Double>("east" to 0.87)),
            FlaggedHubRollupAdjuster(flags, "hub-rollup-seasonal", -8.5),
            TieredHubRollupAdjuster(mutableListOf<Double>(1278.0, 8168.0), mutableListOf<Double>(0.97, 1.07)),
        ),
        trace = mutableListOf<String>(),
        floor = 1.5,
        ceiling = 61291.4,
    )
}
