package com.acme.transport.linehaul.backhaul.backhaulbatch

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual backhaul batch calculation. */
interface ManualBackhaulBatchStep {
    fun describe(): String
    fun adjust(item: ManualBackhaulBatch, value: Double): Double
}

data class LaneManualBackhaulBatchAdjuster(var factors: MutableMap<String, Double>) : ManualBackhaulBatchStep {
    override fun describe(): String = "by lane"

    override fun adjust(item: ManualBackhaulBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class PercentManualBackhaulBatchAdjuster(var percent: Double) : ManualBackhaulBatchStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ManualBackhaulBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ManualBackhaulBatchEndsAtOffsetAdjuster(var weight: Double) : ManualBackhaulBatchStep {
    override fun describe(): String = "plus ends at times $weight"

    override fun adjust(item: ManualBackhaulBatch, value: Double): Double = value + item.endsAt.toDouble() * weight
}

data class CapManualBackhaulBatchAdjuster(var cap: Double) : ManualBackhaulBatchStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualBackhaulBatch, value: Double): Double = minOf(value, cap)
}

data class MinimumManualBackhaulBatchAdjuster(var minimum: Double) : ManualBackhaulBatchStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualBackhaulBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

/** Runs the manual backhaul batch steps in order and keeps a trace of each. */
data class ManualBackhaulBatchEngine(
    var steps: MutableList<ManualBackhaulBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ManualBackhaulBatch): Double {
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

    fun evaluateAll(items: MutableList<ManualBackhaulBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultManualBackhaulBatchEngine(): ManualBackhaulBatchEngine {
    return ManualBackhaulBatchEngine(
        steps = mutableListOf<ManualBackhaulBatchStep>(
            LaneManualBackhaulBatchAdjuster(mutableMapOf<String, Double>("west" to 0.75, "north" to 0.89)),
            PercentManualBackhaulBatchAdjuster(-18.8),
            ManualBackhaulBatchEndsAtOffsetAdjuster(1.27),
            CapManualBackhaulBatchAdjuster(15008.0),
            MinimumManualBackhaulBatchAdjuster(43.0),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 21805.7,
    )
}
