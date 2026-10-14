package com.acme.transport.safety.incident.incidentbaseline

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual incident rollup calculation. */
interface ManualIncidentRollupAdjuster {
    fun describe(): String
    fun adjust(item: ManualIncidentRollup, value: Double): Double
}

data class UnitManualIncidentRollupAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualIncidentRollupAdjuster {
    override fun describe(): String = "by unit"

    override fun adjust(item: ManualIncidentRollup, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapManualIncidentRollupAdjuster(var cap: Double) : ManualIncidentRollupAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualIncidentRollup, value: Double): Double = minOf(value, cap)
}

data class MinimumManualIncidentRollupAdjuster(var minimum: Double) : ManualIncidentRollupAdjuster {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualIncidentRollup, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual incident rollup steps in order and keeps a trace of each. */
data class ManualIncidentRollupEngine(
    var steps: MutableList<ManualIncidentRollupAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualIncidentRollup): Double {
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

    fun evaluateAll(items: MutableList<ManualIncidentRollup>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualIncidentRollupEngine(): ManualIncidentRollupEngine {
    return ManualIncidentRollupEngine(
        steps = mutableListOf<ManualIncidentRollupAdjuster>(
            UnitManualIncidentRollupAdjuster(
                mutableMapOf<String, Double>("central" to 1.4, "retail" to 1.2, "east" to 0.86),
            ),
            CapManualIncidentRollupAdjuster(31045.0),
            MinimumManualIncidentRollupAdjuster(4.4),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 45452.6,
    )
}
