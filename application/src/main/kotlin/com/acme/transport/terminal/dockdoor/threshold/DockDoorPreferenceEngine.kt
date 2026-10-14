package com.acme.transport.terminal.dockdoor.threshold

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the dock door preference calculation. */
interface DockDoorPreferenceAdjuster {
    fun describe(): String
    fun adjust(item: DockDoorPreference, value: Double): Double
}

data class DockDoorPreferenceStartsAtOffsetAdjuster(var weight: Double) : DockDoorPreferenceAdjuster {
    override fun describe(): String = "plus starts at times $weight"

    override fun adjust(item: DockDoorPreference, value: Double): Double = value + item.startsAt.toDouble() * weight
}

data class TieredDockDoorPreferenceAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : DockDoorPreferenceAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: DockDoorPreference, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LabelDockDoorPreferenceAdjuster(
    var factors: MutableMap<String, Double>,
) : DockDoorPreferenceAdjuster {
    override fun describe(): String = "by label"

    override fun adjust(item: DockDoorPreference, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapDockDoorPreferenceAdjuster(var cap: Double) : DockDoorPreferenceAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: DockDoorPreference, value: Double): Double = minOf(value, cap)
}

/** Runs the dock door preference steps in order and keeps a trace of each. */
data class DockDoorPreferenceEngine(
    var steps: MutableList<DockDoorPreferenceAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: DockDoorPreference): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DockDoorPreference>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultDockDoorPreferenceEngine(): DockDoorPreferenceEngine {
    return DockDoorPreferenceEngine(
        steps = mutableListOf<DockDoorPreferenceAdjuster>(
            DockDoorPreferenceStartsAtOffsetAdjuster(1.78),
            TieredDockDoorPreferenceAdjuster(
                mutableListOf<Double>(4192.0, 9584.0),
                mutableListOf<Double>(1.09, 1.12),
            ),
            LabelDockDoorPreferenceAdjuster(mutableMapOf<String, Double>("east" to 0.74, "retail" to 0.95)),
            CapDockDoorPreferenceAdjuster(1050.0),
        ),
        trace = mutableListOf<String>(),
        floor = 3.7,
        ceiling = 41921.2,
    )
}
