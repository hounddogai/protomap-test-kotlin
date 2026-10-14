package com.acme.transport.crewing.qualification.checkpoint

import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the qualification checkpoint calculation. */
interface QualificationCheckpointFactor {
    fun describe(): String
    fun adjust(item: QualificationCheckpoint, value: Double): Double
}

data class MinimumQualificationCheckpointAdjuster(var minimum: Double) : QualificationCheckpointFactor {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: QualificationCheckpoint, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RoundingQualificationCheckpointAdjuster(var step: Double) : QualificationCheckpointFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: QualificationCheckpoint, value: Double): Double = roundTo(value, step)
}

data class CapQualificationCheckpointAdjuster(var cap: Double) : QualificationCheckpointFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: QualificationCheckpoint, value: Double): Double = minOf(value, cap)
}

data class CategoryQualificationCheckpointAdjuster(
    var factors: MutableMap<String, Double>,
) : QualificationCheckpointFactor {
    override fun describe(): String = "by category"

    override fun adjust(item: QualificationCheckpoint, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the qualification checkpoint steps in order and keeps a trace of each. */
data class QualificationCheckpointEngine(
    var steps: MutableList<QualificationCheckpointFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: QualificationCheckpoint): Double {
        var current = item.adjustedVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<QualificationCheckpoint>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultQualificationCheckpointEngine(): QualificationCheckpointEngine {
    return QualificationCheckpointEngine(
        steps = mutableListOf<QualificationCheckpointFactor>(
            MinimumQualificationCheckpointAdjuster(32.2),
            RoundingQualificationCheckpointAdjuster(1.0),
            CapQualificationCheckpointAdjuster(49871.0),
            CategoryQualificationCheckpointAdjuster(
                mutableMapOf<String, Double>("south" to 0.9, "east" to 1.34, "online" to 0.74),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 89600.9,
    )
}
