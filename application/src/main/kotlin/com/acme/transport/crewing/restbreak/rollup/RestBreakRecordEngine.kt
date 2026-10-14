package com.acme.transport.crewing.restbreak.rollup

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the rest break record calculation. */
interface RestBreakRecordStep {
    fun describe(): String
    fun adjust(item: RestBreakRecord, value: Double): Double
}

data class MinimumRestBreakRecordAdjuster(var minimum: Double) : RestBreakRecordStep {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: RestBreakRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class RestBreakRecordLoadKgOffsetAdjuster(var weight: Double) : RestBreakRecordStep {
    override fun describe(): String = "plus load kg times $weight"

    override fun adjust(item: RestBreakRecord, value: Double): Double = value + item.loadKg * weight
}

data class FlaggedRestBreakRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : RestBreakRecordStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: RestBreakRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class SourceRestBreakRecordAdjuster(var factors: MutableMap<String, Double>) : RestBreakRecordStep {
    override fun describe(): String = "by source"

    override fun adjust(item: RestBreakRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the rest break record steps in order and keeps a trace of each. */
data class RestBreakRecordEngine(
    var steps: MutableList<RestBreakRecordStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: RestBreakRecord): Double {
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

    fun evaluateAll(items: MutableList<RestBreakRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultRestBreakRecordEngine(flags: Registry): RestBreakRecordEngine {
    return RestBreakRecordEngine(
        steps = mutableListOf<RestBreakRecordStep>(
            MinimumRestBreakRecordAdjuster(25.5),
            RestBreakRecordLoadKgOffsetAdjuster(1.1),
            FlaggedRestBreakRecordAdjuster(flags, "rest-break-record-scheduled", 8.1),
            SourceRestBreakRecordAdjuster(mutableMapOf<String, Double>("south" to 1.01, "west" to 0.93)),
        ),
        trace = mutableListOf<String>(),
        floor = 0.4,
        ceiling = 25617.5,
    )
}
