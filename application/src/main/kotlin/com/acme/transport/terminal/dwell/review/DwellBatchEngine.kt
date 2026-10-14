package com.acme.transport.terminal.dwell.review

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the dwell batch calculation. */
interface DwellBatchAdjuster {
    fun describe(): String
    fun adjust(item: DwellBatch, value: Double): Double
}

data class RoundingDwellBatchAdjuster(var step: Double) : DwellBatchAdjuster {
    override fun describe(): String = "round to $step"

    override fun adjust(item: DwellBatch, value: Double): Double = roundTo(value, step)
}

data class DwellBatchDurationMinutesOffsetAdjuster(var weight: Double) : DwellBatchAdjuster {
    override fun describe(): String = "plus duration minutes times $weight"

    override fun adjust(item: DwellBatch, value: Double): Double = value + item.durationMinutes.toDouble() * weight
}

data class FlaggedDwellBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : DwellBatchAdjuster {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: DwellBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentDwellBatchAdjuster(var percent: Double) : DwellBatchAdjuster {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: DwellBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the dwell batch steps in order and keeps a trace of each. */
data class DwellBatchEngine(
    var steps: MutableList<DwellBatchAdjuster>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: DwellBatch): Double {
        var current = item.netLoad()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<DwellBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultDwellBatchEngine(flags: Registry): DwellBatchEngine {
    return DwellBatchEngine(
        steps = mutableListOf<DwellBatchAdjuster>(
            RoundingDwellBatchAdjuster(0.1),
            DwellBatchDurationMinutesOffsetAdjuster(0.23),
            FlaggedDwellBatchAdjuster(flags, "dwell-batch-rolling", -7.3),
            PercentDwellBatchAdjuster(-3.9),
        ),
        trace = mutableListOf<String>(),
        floor = 0.8,
        ceiling = 86398.0,
    )
}
