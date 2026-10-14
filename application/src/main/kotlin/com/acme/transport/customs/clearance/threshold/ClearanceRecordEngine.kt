package com.acme.transport.customs.clearance.threshold

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the clearance record calculation. */
interface ClearanceRecordModifier {
    fun describe(): String
    fun adjust(item: ClearanceRecord, value: Double): Double
}

data class MinimumClearanceRecordAdjuster(var minimum: Double) : ClearanceRecordModifier {
    override fun describe(): String = "at least $minimum"

    override fun adjust(item: ClearanceRecord, value: Double): Double {
        if (value < minimum) {
            return minimum
        }
        return value
    }
}

data class TieredClearanceRecordAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ClearanceRecordModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ClearanceRecord, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class RoundingClearanceRecordAdjuster(var step: Double) : ClearanceRecordModifier {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ClearanceRecord, value: Double): Double = roundTo(value, step)
}

data class PercentClearanceRecordAdjuster(var percent: Double) : ClearanceRecordModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ClearanceRecord, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class LaneClearanceRecordAdjuster(var factors: MutableMap<String, Double>) : ClearanceRecordModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: ClearanceRecord, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class FlaggedClearanceRecordAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ClearanceRecordModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ClearanceRecord, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

/** Runs the clearance record steps in order and keeps a trace of each. */
data class ClearanceRecordEngine(
    var steps: MutableList<ClearanceRecordModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun price(item: ClearanceRecord): Double {
        var current = item.netExposure()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ClearanceRecordModifier = steps[index]
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

    fun evaluateAll(items: MutableList<ClearanceRecord>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = price(item)
        }
        return results
    }
}

fun defaultClearanceRecordEngine(flags: Registry): ClearanceRecordEngine {
    return ClearanceRecordEngine(
        steps = mutableListOf<ClearanceRecordModifier>(
            MinimumClearanceRecordAdjuster(23.2),
            TieredClearanceRecordAdjuster(
                mutableListOf<Double>(703.0, 7119.0, 8642.0),
                mutableListOf<Double>(0.83, 0.83, 1.15),
            ),
            RoundingClearanceRecordAdjuster(5.0),
            PercentClearanceRecordAdjuster(-24.4),
            LaneClearanceRecordAdjuster(mutableMapOf<String, Double>("retail" to 1.0)),
            FlaggedClearanceRecordAdjuster(flags, "clearance-record-pending", 7.5),
        ),
        trace = mutableListOf<String>(),
        floor = 0.3,
        ceiling = 42049.0,
    )
}
