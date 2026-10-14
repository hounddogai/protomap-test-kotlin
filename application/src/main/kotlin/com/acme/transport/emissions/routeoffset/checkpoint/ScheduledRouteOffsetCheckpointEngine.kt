package com.acme.transport.emissions.routeoffset.checkpoint

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled route offset checkpoint calculation. */
interface ScheduledRouteOffsetCheckpointFactor {
    fun describe(): String
    fun adjust(item: ScheduledRouteOffsetCheckpoint, value: Double): Double
}

data class FlaggedScheduledRouteOffsetCheckpointAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledRouteOffsetCheckpointFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledRouteOffsetCheckpoint, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class DepotScheduledRouteOffsetCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledRouteOffsetCheckpointFactor {
    override fun describe(): String = "by depot"

    override fun adjust(item: ScheduledRouteOffsetCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentScheduledRouteOffsetCheckpointAdjuster(
    var percent: Double,
) : ScheduledRouteOffsetCheckpointFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledRouteOffsetCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class TieredScheduledRouteOffsetCheckpointAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledRouteOffsetCheckpointFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledRouteOffsetCheckpoint, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the scheduled route offset checkpoint steps in order and keeps a trace of each. */
data class ScheduledRouteOffsetCheckpointEngine(
    var steps: MutableList<ScheduledRouteOffsetCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ScheduledRouteOffsetCheckpoint): Double {
        var current = item.projectedLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledRouteOffsetCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultScheduledRouteOffsetCheckpointEngine(flags: Registry): ScheduledRouteOffsetCheckpointEngine {
    return ScheduledRouteOffsetCheckpointEngine(
        steps = mutableListOf<ScheduledRouteOffsetCheckpointFactor>(
            FlaggedScheduledRouteOffsetCheckpointAdjuster(
                flags,
                "scheduled-route-offset-checkpoint-tiered",
                -2.5,
            ),
            DepotScheduledRouteOffsetCheckpointAdjuster(
                mutableMapOf<String, Double>("central" to 1.02, "west" to 1.39, "retail" to 1.11),
            ),
            PercentScheduledRouteOffsetCheckpointAdjuster(-9.9),
            TieredScheduledRouteOffsetCheckpointAdjuster(
                mutableListOf<Double>(213.0, 1227.0, 2847.0, 4802.0),
                mutableListOf<Double>(1.11, 1.18, 0.89, 0.84),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 63928.0,
    )
}
