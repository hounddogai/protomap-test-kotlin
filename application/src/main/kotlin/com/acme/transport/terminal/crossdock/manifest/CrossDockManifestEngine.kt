package com.acme.transport.terminal.crossdock.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the cross dock manifest calculation. */
interface CrossDockManifestAdjuster {
    fun describe(): String
    fun adjust(item: CrossDockManifest, value: Double): Double
}

data class CrossDockManifestCapacityOffsetAdjuster(var weight: Double) : CrossDockManifestAdjuster {
    override fun describe(): String = "plus capacity times $weight"

    override fun adjust(item: CrossDockManifest, value: Double): Double = value + item.capacity.toDouble() * weight
}

data class MinimumCrossDockManifestAdjuster(var minimum: Double) : CrossDockManifestAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: CrossDockManifest, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class DepotCrossDockManifestAdjuster(var factors: MutableMap<String, Double>) : CrossDockManifestAdjuster {
    override fun describe(): String = "by depot"

    override fun adjust(item: CrossDockManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedCrossDockManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : CrossDockManifestAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: CrossDockManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class TieredCrossDockManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : CrossDockManifestAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: CrossDockManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class PercentCrossDockManifestAdjuster(var percent: Double) : CrossDockManifestAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: CrossDockManifest, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the cross dock manifest steps in order and keeps a trace of each. */
data class CrossDockManifestEngine(
    var steps: MutableList<CrossDockManifestAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: CrossDockManifest): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<CrossDockManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultCrossDockManifestEngine(flags: Registry): CrossDockManifestEngine {
    return CrossDockManifestEngine(
        steps = mutableListOf<CrossDockManifestAdjuster>(
            CrossDockManifestCapacityOffsetAdjuster(1.33),
            MinimumCrossDockManifestAdjuster(10.5),
            DepotCrossDockManifestAdjuster(
                mutableMapOf<String, Double>("south" to 0.86, "central" to 0.95, "west" to 1.15),
            ),
            FlaggedCrossDockManifestAdjuster(flags, "cross-dock-manifest-pending", -4.4),
            TieredCrossDockManifestAdjuster(
                mutableListOf<Double>(1342.0, 7734.0),
                mutableListOf<Double>(1.12, 0.88),
            ),
            PercentCrossDockManifestAdjuster(-7.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.6,
        ceiling = 39069.0,
    )
}
