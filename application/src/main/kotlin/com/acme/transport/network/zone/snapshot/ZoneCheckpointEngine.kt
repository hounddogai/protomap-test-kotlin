package com.acme.transport.network.zone.snapshot

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the zone checkpoint calculation. */
interface ZoneCheckpointFactor {
    fun describe(): String
    fun adjust(item: ZoneCheckpoint, value: Double): Double
}

data class FlaggedZoneCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ZoneCheckpointFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ZoneCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class MinimumZoneCheckpointAdjuster(var minimum: Double) : ZoneCheckpointFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ZoneCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class ZoneCheckpointMarginOffsetAdjuster(var weight: Double) : ZoneCheckpointFactor {
    override fun describe(): String = "plus margin times $weight"

    override fun adjust(item: ZoneCheckpoint, value: Double): Double = value + item.margin * weight
}

data class TieredZoneCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ZoneCheckpointFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ZoneCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentZoneCheckpointAdjuster(var percent: Double) : ZoneCheckpointFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ZoneCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the zone checkpoint steps in order and keeps a trace of each. */
data class ZoneCheckpointEngine(
    var steps: MutableList<ZoneCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ZoneCheckpoint): Double {
        var current = item.netValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ZoneCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultZoneCheckpointEngine(flags: Registry): ZoneCheckpointEngine {
    return ZoneCheckpointEngine(
        steps = mutableListOf<ZoneCheckpointFactor>(
            FlaggedZoneCheckpointAdjuster(flags, "zone-checkpoint-batched", 6.3),
            MinimumZoneCheckpointAdjuster(28.3),
            ZoneCheckpointMarginOffsetAdjuster(0.93),
            TieredZoneCheckpointAdjuster(
                mutableListOf<Double>(2455.0, 4776.0, 5096.0, 7663.0),
                mutableListOf<Double>(1.17, 0.87, 1.18, 1.01),
            ),
            PercentZoneCheckpointAdjuster(-3.8),
        ),
        trace = mutableListOf<String>(),
        floor = 0.2,
        ceiling = 83587.4,
    )
}
