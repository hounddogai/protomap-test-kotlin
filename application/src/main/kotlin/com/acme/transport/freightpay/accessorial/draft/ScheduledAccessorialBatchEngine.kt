package com.acme.transport.freightpay.accessorial.draft

import com.acme.transport.kit.flags.Registry
import com.acme.transport.kit.numbers.roundTo

/** One step of the scheduled accessorial batch calculation. */
interface ScheduledAccessorialBatchStep {
    fun describe(): String
    fun adjust(item: ScheduledAccessorialBatch, value: Double): Double
}

data class RoundingScheduledAccessorialBatchAdjuster(var step: Double) : ScheduledAccessorialBatchStep {
    override fun describe(): String = "round to $step"

    override fun adjust(item: ScheduledAccessorialBatch, value: Double): Double = roundTo(value, step)
}

data class CapScheduledAccessorialBatchAdjuster(var cap: Double) : ScheduledAccessorialBatchStep {
    override fun describe(): String = "cap at $cap"

    override fun adjust(item: ScheduledAccessorialBatch, value: Double): Double = minOf(value, cap)
}

data class NameScheduledAccessorialBatchAdjuster(
    var factors: MutableMap<String, Double>,
) : ScheduledAccessorialBatchStep {
    override fun describe(): String = "by name"

    override fun adjust(item: ScheduledAccessorialBatch, value: Double): Double {
        val factor = factors[item.groupKey()] ?: return value
        return value * factor
    }
}

data class TieredScheduledAccessorialBatchAdjuster(
    var bounds: MutableList<Double>,
    var rates: MutableList<Double>,
) : ScheduledAccessorialBatchStep {
    override fun describe(): String = "${bounds.size} tiers"

    override fun adjust(item: ScheduledAccessorialBatch, value: Double): Double {
        for (index in 0 until bounds.size) {
            if (value <= bounds[index] && index < rates.size) {
                return value * rates[index]
            }
        }
        return value
    }
}

data class FlaggedScheduledAccessorialBatchAdjuster(
    var flags: Registry,
    var flag: String,
    var percent: Double,
) : ScheduledAccessorialBatchStep {
    override fun describe(): String = "$percent when $flag is on"

    override fun adjust(item: ScheduledAccessorialBatch, value: Double): Double {
        if (!flags.enabled(flag, item.code)) {
            return value
        }
        return value * (1.0 + percent / 100.0)
    }
}

data class PercentScheduledAccessorialBatchAdjuster(var percent: Double) : ScheduledAccessorialBatchStep {
    override fun describe(): String = "$percent% change"

    override fun adjust(item: ScheduledAccessorialBatch, value: Double): Double = value * (1.0 + percent / 100.0)
}

/** Runs the scheduled accessorial batch steps in order and keeps a trace of each. */
data class ScheduledAccessorialBatchEngine(
    var steps: MutableList<ScheduledAccessorialBatchStep>,
    var trace: MutableList<String>,
    var floor: Double,
    var ceiling: Double,
) {
    fun compute(item: ScheduledAccessorialBatch): Double {
        var current = item.projectedVolume()
        trace = mutableListOf<String>()
        for (index in 0 until steps.size) {
            val step: ScheduledAccessorialBatchStep = steps[index]
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

    fun evaluateAll(items: MutableList<ScheduledAccessorialBatch>): MutableMap<String, Double> {
        val results = mutableMapOf<String, Double>()
        for (item in items) {
            results[item.code] = compute(item)
        }
        return results
    }
}

fun defaultScheduledAccessorialBatchEngine(flags: Registry): ScheduledAccessorialBatchEngine {
    return ScheduledAccessorialBatchEngine(
        steps = mutableListOf<ScheduledAccessorialBatchStep>(
            RoundingScheduledAccessorialBatchAdjuster(0.5),
            CapScheduledAccessorialBatchAdjuster(29378.0),
            NameScheduledAccessorialBatchAdjuster(mutableMapOf<String, Double>("south" to 1.0)),
            TieredScheduledAccessorialBatchAdjuster(
                mutableListOf<Double>(6799.0, 7203.0),
                mutableListOf<Double>(0.93, 1.18),
            ),
            FlaggedScheduledAccessorialBatchAdjuster(flags, "scheduled-accessorial-batch-capped", 9.6),
            PercentScheduledAccessorialBatchAdjuster(-21.7),
        ),
        trace = mutableListOf<String>(),
        floor = 0.5,
        ceiling = 94206.9,
    )
}
