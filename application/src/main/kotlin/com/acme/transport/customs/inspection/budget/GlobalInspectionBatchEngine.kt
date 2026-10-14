package com.acme.transport.customs.inspection.budget

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the global inspection batch calculation. */
interface GlobalInspectionBatchAdjuster {
    fun describe(): String
    fun adjust(item: GlobalInspectionBatch, value: Double): Double
}

data class TieredGlobalInspectionBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : GlobalInspectionBatchAdjuster {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: GlobalInspectionBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class CapGlobalInspectionBatchAdjuster(var cap: Double) : GlobalInspectionBatchAdjuster {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: GlobalInspectionBatch, value: Double): Double = minOf(value, cap)
}

data class GlobalInspectionBatchSequenceOffsetAdjuster(var weight: Double) : GlobalInspectionBatchAdjuster {
    override fun describe(): String = "plus sequence times $weight"

    override fun adjust(item: GlobalInspectionBatch, value: Double): Double {
        return value + item.sequence.toDouble() * weight
    }
}

data class NameGlobalInspectionBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : GlobalInspectionBatchAdjuster {
    override fun describe(): String = "by name"

    override fun adjust(item: GlobalInspectionBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedGlobalInspectionBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : GlobalInspectionBatchAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: GlobalInspectionBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the global inspection batch steps in order and keeps a trace of each. */
data class GlobalInspectionBatchEngine(
    var steps: MutableList<GlobalInspectionBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: GlobalInspectionBatch): Double {
        var current = item.effectiveVolume()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<GlobalInspectionBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultGlobalInspectionBatchEngine(flags: Registry): GlobalInspectionBatchEngine {
    return GlobalInspectionBatchEngine(
        steps = mutableListOf<GlobalInspectionBatchAdjuster>(
            TieredGlobalInspectionBatchAdjuster(
                mutableListOf<Double>(5007.0, 7801.0),
                mutableListOf<Double>(1.15, 0.86),
            ),
            CapGlobalInspectionBatchAdjuster(47973.0),
            GlobalInspectionBatchSequenceOffsetAdjuster(0.59),
            NameGlobalInspectionBatchAdjuster(mutableMapOf<String, Double>("west" to 0.95, "east" to 1.08)),
            FlaggedGlobalInspectionBatchAdjuster(flags, "global-inspection-batch-capped", -9.3),
        ),
        trace = mutableListOf<String>(),
        floor = 4.2,
        ceiling = 57536.8,
    )
}
