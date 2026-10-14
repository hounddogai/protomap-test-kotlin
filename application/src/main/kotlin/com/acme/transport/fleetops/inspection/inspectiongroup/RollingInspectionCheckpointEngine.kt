package com.acme.transport.fleetops.inspection.inspectiongroup

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the rolling inspection checkpoint calculation. */
interface RollingInspectionCheckpointModifier {
    fun describe(): String
    fun adjust(item: RollingInspectionCheckpoint, value: Double): Double
}

data class CapRollingInspectionCheckpointAdjuster(var cap: Double) : RollingInspectionCheckpointModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: RollingInspectionCheckpoint, value: Double): Double = minOf(value, cap)
}

data class UnitRollingInspectionCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : RollingInspectionCheckpointModifier {
    override fun describe(): String = "by unit"

    override fun adjust(item: RollingInspectionCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class RollingInspectionCheckpointBatchSizeOffsetAdjuster(
    var weight: Double,
) : RollingInspectionCheckpointModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: RollingInspectionCheckpoint, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class RoundingRollingInspectionCheckpointAdjuster(var step: Double) : RollingInspectionCheckpointModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: RollingInspectionCheckpoint, value: Double): Double = roundTo(value, step)
}

data class PercentRollingInspectionCheckpointAdjuster(
    var percent: Double,
) : RollingInspectionCheckpointModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: RollingInspectionCheckpoint, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the rolling inspection checkpoint steps in order and keeps a trace of each. */
data class RollingInspectionCheckpointEngine(
    var steps: MutableList<RollingInspectionCheckpointModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: RollingInspectionCheckpoint): Double {
        var current = item.effectiveValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<RollingInspectionCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultRollingInspectionCheckpointEngine(): RollingInspectionCheckpointEngine {
    return RollingInspectionCheckpointEngine(
        steps = mutableListOf<RollingInspectionCheckpointModifier>(
            CapRollingInspectionCheckpointAdjuster(35679.0),
            UnitRollingInspectionCheckpointAdjuster(
                mutableMapOf<String, Double>("online" to 1.12, "south" to 0.86, "east" to 0.99),
            ),
            RollingInspectionCheckpointBatchSizeOffsetAdjuster(1.68),
            RoundingRollingInspectionCheckpointAdjuster(0.1),
            PercentRollingInspectionCheckpointAdjuster(5.7),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 25948.1,
    )
}
