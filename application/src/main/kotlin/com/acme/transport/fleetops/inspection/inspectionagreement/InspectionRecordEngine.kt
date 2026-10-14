package com.acme.transport.fleetops.inspection.inspectionagreement

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** One step of the inspection record calculation. */
interface InspectionRecordModifier {
    fun describe(): String
    fun adjust(item: InspectionRecord, value: Double): Double
}

data class MinimumInspectionRecordAdjuster(var minimum: Double) : InspectionRecordModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: InspectionRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredInspectionRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : InspectionRecordModifier {
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

data class PercentInspectionRecordAdjuster(var percent: Double) : InspectionRecordModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: InspectionRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class RoundingInspectionRecordAdjuster(var step: Double) : InspectionRecordModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: InspectionRecord, value: Double): Double = roundTo(value, step)
}

data class InspectionRecordMaximumOffsetAdjuster(var weight: Double) : InspectionRecordModifier {
    override fun describe(): String = "plus maximum times $weight"

    override fun adjust(item: InspectionRecord, value: Double): Double = value + item.maximum * weight
}

data class FlaggedInspectionRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : InspectionRecordModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: InspectionRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the inspection record steps in order and keeps a trace of each. */
data class InspectionRecordEngine(
    var steps: MutableList<InspectionRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun run(item: InspectionRecord): Double {
        var current = item.netTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
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
        steps = mutableListOf<InspectionRecordModifier>(
            MinimumInspectionRecordAdjuster(1.0),
            TieredInspectionRecordAdjuster(
                mutableListOf<Double>(2444.0, 3726.0, 8260.0),
                mutableListOf<Double>(1.13, 1.07, 1.01),
            ),
            PercentInspectionRecordAdjuster(-1.8),
            RoundingInspectionRecordAdjuster(5.0),
            InspectionRecordMaximumOffsetAdjuster(0.53),
            FlaggedInspectionRecordAdjuster(flags, "inspection-record-tiered", -1.3),
        ),
        trace = mutableListOf<String>(),
        floor = 1.6,
        ceiling = 22557.2,
    )
}
