package com.acme.transport.customs.inspection.batch

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the inspection record calculation. */
interface InspectionRecordFactor {
    fun describe(): String
    fun adjust(item: InspectionRecord, value: Double): Double
}

data class RoundingInspectionRecordAdjuster(var step: Double) : InspectionRecordFactor {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionRecord, value: Double): Double = roundTo(value, step)
}

data class FlaggedInspectionRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionRecordFactor {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapInspectionRecordAdjuster(var cap: Double) : InspectionRecordFactor {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: InspectionRecord, value: Double): Double = minOf(value, cap)
}

data class TieredInspectionRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionRecordFactor {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: InspectionRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

/** Runs the inspection record steps in order and keeps a trace of each. */
data class InspectionRecordEngine(
    var steps: MutableList<InspectionRecordFactor>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: InspectionRecord): Double {
        var current = item.measuredValue()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: InspectionRecordFactor = steps[index]
            val before = current
            current = step.adjust(item, current)
            if (before != current) {
                trace.add("${index + 1}. ${step.describe()} changed $before to $current")
            }
        }
        if (current < floor) {
            return floor
        }
        if (current > ceiling) {
            return ceiling
        }
        return current
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<InspectionRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = run(item)
        }
        return results
    }
}

fun defaultInspectionRecordEngine(flags: Registry): InspectionRecordEngine {
    return InspectionRecordEngine(
        steps = mutableListOf<InspectionRecordFactor>(
            RoundingInspectionRecordAdjuster(0.5),
            FlaggedInspectionRecordAdjuster(flags, "inspection-record-manual", -5.9),
            CapInspectionRecordAdjuster(40612.0),
            TieredInspectionRecordAdjuster(
                mutableListOf<Double>(3500.0, 6684.0, 7267.0, 9532.0),
                mutableListOf<Double>(1.19, 1.11, 0.82, 0.88),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 1.2,
        ceiling = 43708.1,
    )
}
