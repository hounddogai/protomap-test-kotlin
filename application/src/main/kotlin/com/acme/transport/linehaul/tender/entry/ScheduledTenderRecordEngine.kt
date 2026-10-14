package com.acme.transport.linehaul.tender.entry

import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled tender record calculation. */
interface ScheduledTenderRecordFactor {
    fun describe(): String
    fun adjust(item: ScheduledTenderRecord, value: Double): Double
}

data class TieredScheduledTenderRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledTenderRecordFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledTenderRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class ScheduledTenderRecordCreatedAtOffsetAdjuster(var weight: Double) : ScheduledTenderRecordFactor {
    override fun describe(): String = "plus created at times $weight"

    override fun adjust(item: ScheduledTenderRecord, value: Double): Double {
        return value + item.createdAt.toDouble() * weight
    }
}

data class TitleScheduledTenderRecordAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledTenderRecordFactor {
    override fun describe(): String = "by title"

    override fun adjust(item: ScheduledTenderRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class CapScheduledTenderRecordAdjuster(var cap: Double) : ScheduledTenderRecordFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledTenderRecord, value: Double): Double = minOf(value, cap)
}

/** Runs the scheduled tender record steps in order and keeps a trace of each. */
data class ScheduledTenderRecordEngine(
    var steps: MutableList<ScheduledTenderRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledTenderRecord): Double {
        var current = item.effectiveExposure()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledTenderRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledTenderRecordEngine(): ScheduledTenderRecordEngine {
    return ScheduledTenderRecordEngine(
        steps = mutableListOf<ScheduledTenderRecordFactor>(
            TieredScheduledTenderRecordAdjuster(
                mutableListOf<Double>(2725.0, 7409.0, 7865.0, 9305.0),
                mutableListOf<Double>(1.06, 1.07, 1.13, 0.9),
            ),
            ScheduledTenderRecordCreatedAtOffsetAdjuster(1.89),
            TitleScheduledTenderRecordAdjuster(
                mutableMapOf<String, Double>("west" to 1.1, "retail" to 0.8, "east" to 1.02),
            ),
            CapScheduledTenderRecordAdjuster(37816.0),
        ),
        trace = mutableListOf<String>(),
        floor = 2.5,
        ceiling = 5077.3,
    )
}
