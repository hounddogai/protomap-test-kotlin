package com.acme.transport.fleetops.inspection.schedule

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the inspection checkpoint calculation. */
interface InspectionCheckpointFactor {
    fun describe(): String
    fun adjust(item: InspectionCheckpoint, value: Double): Double
}

data class RoundingInspectionCheckpointAdjuster(var step: Double) : InspectionCheckpointFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionCheckpoint, value: Double): Double = roundTo(value, step)
}

data class CapInspectionCheckpointAdjuster(var cap: Double) : InspectionCheckpointFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionCheckpoint, value: Double): Double = minOf(value, cap)
}

data class TitleInspectionCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : InspectionCheckpointFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: InspectionCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the inspection checkpoint steps in order and keeps a trace of each. */
data class InspectionCheckpointEngine(
    var steps: MutableList<InspectionCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: InspectionCheckpoint): Double {
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

    fun evaluateAll(items: MutableList<InspectionCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultInspectionCheckpointEngine(): InspectionCheckpointEngine {
    return InspectionCheckpointEngine(
        steps = mutableListOf<InspectionCheckpointFactor>(
            RoundingInspectionCheckpointAdjuster(0.25),
            CapInspectionCheckpointAdjuster(39515.0),
            TitleInspectionCheckpointAdjuster(
                mutableMapOf<String, Double>("retail" to 1.28, "central" to 0.85, "east" to 1.34),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 2.1,
        ceiling = 52786.7,
    )
}
