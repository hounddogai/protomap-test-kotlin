package com.acme.transport.safety.violation.manifest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the manual violation batch calculation. */
interface ManualViolationBatchModifier {
    fun describe(): String
    fun adjust(item: ManualViolationBatch, value: Double): Double
}

data class CapManualViolationBatchAdjuster(var cap: Double) : ManualViolationBatchModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ManualViolationBatch, value: Double): Double = minOf(value, cap)
}

data class MinimumManualViolationBatchAdjuster(var minimum: Double) : ManualViolationBatchModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ManualViolationBatch, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class FlaggedManualViolationBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ManualViolationBatchModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ManualViolationBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class LabelManualViolationBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : ManualViolationBatchModifier {
    override fun describe(): String = "by label"

    override fun adjust(item: ManualViolationBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class ManualViolationBatchBatchSizeOffsetAdjuster(var weight: Double) : ManualViolationBatchModifier {
    override fun describe(): String = "plus batch size times $weight"

    override fun adjust(item: ManualViolationBatch, value: Double): Double {
        return value + item.batchSize.toDouble() * weight
    }
}

data class TieredManualViolationBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ManualViolationBatchModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ManualViolationBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the manual violation batch steps in order and keeps a trace of each. */
data class ManualViolationBatchEngine(
    var steps: MutableList<ManualViolationBatchModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: ManualViolationBatch): Double {
        var current = item.projectedValue()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ManualViolationBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultManualViolationBatchEngine(flags: Registry): ManualViolationBatchEngine {
    return ManualViolationBatchEngine(
        steps = mutableListOf<ManualViolationBatchModifier>(
            CapManualViolationBatchAdjuster(28222.0),
            MinimumManualViolationBatchAdjuster(46.2),
            FlaggedManualViolationBatchAdjuster(flags, "manual-violation-batch-primary", -0.9),
            LabelManualViolationBatchAdjuster(mutableMapOf<String, Double>("retail" to 0.83)),
            ManualViolationBatchBatchSizeOffsetAdjuster(1.94),
            TieredManualViolationBatchAdjuster(
                mutableListOf<Double>(6413.0, 6671.0),
                mutableListOf<Double>(0.99, 1.01),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.7,
        ceiling = 44393.2,
    )
}
