package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the default service area snapshot calculation. */
interface DefaultServiceAreaSnapshotFactor {
    fun describe(): String
    fun adjust(item: DefaultServiceAreaSnapshot, value: Double): Double
}

data class TieredDefaultServiceAreaSnapshotAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DefaultServiceAreaSnapshotFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DefaultServiceAreaSnapshot, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDefaultServiceAreaSnapshotAdjuster(var cap: Double) : DefaultServiceAreaSnapshotFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DefaultServiceAreaSnapshot, value: Double): Double = minOf(value, cap)
}

data class NameDefaultServiceAreaSnapshotAdjuster(
    var factors: MutableMap<String, Double>,
) : DefaultServiceAreaSnapshotFactor {
    override fun describe(): String = "by name"

    override fun adjust(item: DefaultServiceAreaSnapshot, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class MinimumDefaultServiceAreaSnapshotAdjuster(var minimum: Double) : DefaultServiceAreaSnapshotFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: DefaultServiceAreaSnapshot, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedDefaultServiceAreaSnapshotAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DefaultServiceAreaSnapshotFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DefaultServiceAreaSnapshot, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDefaultServiceAreaSnapshotAdjuster(var percent: Double) : DefaultServiceAreaSnapshotFactor {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DefaultServiceAreaSnapshot, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the default service area snapshot steps in order and keeps a trace of each. */
data class DefaultServiceAreaSnapshotEngine(
    var steps: MutableList<DefaultServiceAreaSnapshotFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DefaultServiceAreaSnapshot): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DefaultServiceAreaSnapshot>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDefaultServiceAreaSnapshotEngine(flags: Registry): DefaultServiceAreaSnapshotEngine {
    return DefaultServiceAreaSnapshotEngine(
        steps = mutableListOf<DefaultServiceAreaSnapshotFactor>(
            TieredDefaultServiceAreaSnapshotAdjuster(
                mutableListOf<Double>(368.0, 927.0, 6877.0, 8878.0),
                mutableListOf<Double>(0.92, 0.91, 0.86, 0.89),
            ),
            CapDefaultServiceAreaSnapshotAdjuster(6846.0),
            NameDefaultServiceAreaSnapshotAdjuster(
                mutableMapOf<String, Double>("retail" to 1.22, "online" to 0.72, "central" to 1.25),
            ),
            MinimumDefaultServiceAreaSnapshotAdjuster(49.0),
            FlaggedDefaultServiceAreaSnapshotAdjuster(flags, "default-service-area-snapshot-legacy", -1.2),
            PercentDefaultServiceAreaSnapshotAdjuster(6.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 98545.6,
    )
}
