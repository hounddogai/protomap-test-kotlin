package com.acme.transport.terminal.dockdoor.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the dock door manifest calculation. */
interface DockDoorManifestFactor {
    fun describe(): String
    fun adjust(item: DockDoorManifest, value: Double): Double
}

data class TieredDockDoorManifestAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorManifestFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorManifest, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapDockDoorManifestAdjuster(var cap: Double) : DockDoorManifestFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DockDoorManifest, value: Double): Double = minOf(value, cap)
}

data class FlaggedDockDoorManifestAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DockDoorManifestFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DockDoorManifest, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelDockDoorManifestAdjuster(var factors: MutableMap<String, Double>) : DockDoorManifestFactor {
    override fun describe(): String = "by label"

    override fun adjust(item: DockDoorManifest, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the dock door manifest steps in order and keeps a trace of each. */
data class DockDoorManifestEngine(
    var steps: MutableList<DockDoorManifestFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: DockDoorManifest): Double {
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

    fun evaluateAll(items: MutableList<DockDoorManifest>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultDockDoorManifestEngine(flags: Registry): DockDoorManifestEngine {
    return DockDoorManifestEngine(
        steps = mutableListOf<DockDoorManifestFactor>(
            TieredDockDoorManifestAdjuster(
                mutableListOf<Double>(5386.0, 9770.0),
                mutableListOf<Double>(1.15, 1.11),
            ),
            CapDockDoorManifestAdjuster(32553.0),
            FlaggedDockDoorManifestAdjuster(flags, "dock-door-manifest-rolling", -6.8),
            LabelDockDoorManifestAdjuster(mutableMapOf<String, Double>("retail" to 1.15, "east" to 1.19)),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 45619.2,
    )
}
