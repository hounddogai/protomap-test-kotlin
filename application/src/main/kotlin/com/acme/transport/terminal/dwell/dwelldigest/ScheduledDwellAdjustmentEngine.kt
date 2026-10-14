package com.acme.transport.terminal.dwell.dwelldigest

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.clampFloat
import kotlin.math.round

/** One step of the scheduled dwell adjustment calculation. */
interface ScheduledDwellAdjustmentModifier {
    fun describe(): String
    fun adjust(item: ScheduledDwellAdjustment, value: Double): Double
}

data class FlaggedScheduledDwellAdjustmentAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledDwellAdjustmentModifier {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledDwellAdjustment, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class CapScheduledDwellAdjustmentAdjuster(var cap: Double) : ScheduledDwellAdjustmentModifier {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledDwellAdjustment, value: Double): Double = minOf(value, cap)
}

data class PercentScheduledDwellAdjustmentAdjuster(var percent: Double) : ScheduledDwellAdjustmentModifier {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledDwellAdjustment, value: Double): Double = value * (1.0 + percent / 100.0)
}

data class ScheduledDwellAdjustmentUpdatedAtOffsetAdjuster(
    var weight: Double,
) : ScheduledDwellAdjustmentModifier {
    override fun describe(): String = "plus updated at times $weight"

    override fun adjust(item: ScheduledDwellAdjustment, value: Double): Double {
        return value + item.updatedAt.toDouble() * weight
    }
}

data class TieredScheduledDwellAdjustmentAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledDwellAdjustmentModifier {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledDwellAdjustment, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class LaneScheduledDwellAdjustmentAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledDwellAdjustmentModifier {
    override fun describe(): String = "by lane"

    override fun adjust(item: ScheduledDwellAdjustment, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

/** Runs the scheduled dwell adjustment steps in order and keeps a trace of each. */
data class ScheduledDwellAdjustmentEngine(
    var steps: MutableList<ScheduledDwellAdjustmentModifier>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun evaluate(item: ScheduledDwellAdjustment): Double {
        var current = item.effectiveTotal()
        trace = mutableListOf<String>()
        for (step in steps) {
            val next = step.adjust(item, current)
            trace.add("${step.describe()}: ${round(current * 100) / 100} -> ${round(next * 100) / 100}")
            current = next
        }
        return clampFloat(current, floor, ceiling)
    }

    fun explain(): String = trace.joinToString("\n")

    fun evaluateAll(items: MutableList<ScheduledDwellAdjustment>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = evaluate(item)
        }
        return results
    }
}

fun defaultScheduledDwellAdjustmentEngine(flags: Registry): ScheduledDwellAdjustmentEngine {
    return ScheduledDwellAdjustmentEngine(
        steps = mutableListOf<ScheduledDwellAdjustmentModifier>(
            FlaggedScheduledDwellAdjustmentAdjuster(flags, "scheduled-dwell-adjustment-rolling", -4.3),
            CapScheduledDwellAdjustmentAdjuster(22364.0),
            PercentScheduledDwellAdjustmentAdjuster(-14.4),
            ScheduledDwellAdjustmentUpdatedAtOffsetAdjuster(0.77),
            TieredScheduledDwellAdjustmentAdjuster(
                mutableListOf<Double>(240.0, 842.0, 8602.0),
                mutableListOf<Double>(0.98, 1.04, 0.87),
            ),
            LaneScheduledDwellAdjustmentAdjuster(
                mutableMapOf<String, Double>("online" to 1.29, "north" to 1.09),
            ),
        ),
        trace = mutableListOf<String>(),
        floor = 0.7,
        ceiling = 23919.7,
    )
}
